package com.ezbookkeeping.qa.fixtures;

import com.ezbookkeeping.qa.api.client.AuthClient;
import com.ezbookkeeping.qa.config.AppConfig;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Popula a conta de teste com dados realistas de pelo menos 2 meses:
 * contas, categorias (reusa presets, cria se faltar), tags e transacoes deterministas.
 * Idempotente: elementos que ja existem por nome sao reutilizados.
 */
public final class DataSeeder {

    private static final String TZ = AppConfig.DEFAULT_TIMEZONE;
    private static final int UTC_OFFSET = -180;
    private static final String COMMENT_PREFIX = "SeedQA";

    private static final String CAT_ALIMENTACAO = "Alimentação";
    private static final String CAT_TRANSPORTE_PUBLICO = "Transporte Público";
    private static final String CAT_CARRO = "Despesas com Carro Pessoal";
    private static final String CAT_ALUGUEL = "Aluguel e Hipoteca";
    private static final String CAT_CONTAS_CONSUMO = "Contas de Consumo";
    private static final String CAT_CINEMA = "Cinema e Shows";
    private static final String CAT_ASSINATURAS = "Assinaturas";
    private static final String CAT_MEDICAMENTOS = "Medicamentos";
    private static final String CAT_LIVROS = "Livros, Jornais e Revistas";
    private static final String CAT_SALARIO = "Salário";
    private static final String CAT_RENDA_EXTRA = "Renda Extra";
    private static final String CAT_TRANSFERENCIA = "Transferência Bancária";
    private static final String CAT_FATURA_CARTAO = "Fatura do Cartão de Crédito";

    private static final String TAG_ESSENCIAL = "essencial";
    private static final String TAG_LAZER = "lazer";
    private static final String TAG_FIXO = "fixo";
    private static final String TAG_CARRO = "carro";
    private static final String TAG_TRABALHO = "trabalho";
    private static final String TAG_ESPORADICO = "esporadico";

    private static final int TYPE_INCOME = 2;
    private static final int TYPE_EXPENSE = 3;
    private static final int TYPE_TRANSFER = 4;

    private static final List<Map<String, String>> FALLBACK_CATEGORIES = List.of(
            Map.of("parent", "Renda", "child", CAT_SALARIO, "type", "1", "icon", "2010", "color", "ff6b22"),
            Map.of("parent", "Renda", "child", CAT_RENDA_EXTRA, "type", "1", "icon", "2080", "color", "ff6b22"),
            Map.of("parent", "Alimentação e Bebidas", "child", CAT_ALIMENTACAO, "type", "2", "icon", "2", "color", "ff6b22"),
            Map.of("parent", "Transporte", "child", CAT_TRANSPORTE_PUBLICO, "type", "2", "icon", "310", "color", "009688"),
            Map.of("parent", "Transporte", "child", CAT_CARRO, "type", "2", "icon", "330", "color", "009688"),
            Map.of("parent", "Moradia e Utensílios Domésticos", "child", CAT_ALUGUEL, "type", "2", "icon", "290", "color", "000000"),
            Map.of("parent", "Moradia e Utensílios Domésticos", "child", CAT_CONTAS_CONSUMO, "type", "2", "icon", "270", "color", "000000"),
            Map.of("parent", "Entretenimento", "child", CAT_CINEMA, "type", "2", "icon", "550", "color", "ff2d55"),
            Map.of("parent", "Entretenimento", "child", CAT_ASSINATURAS, "type", "2", "icon", "570", "color", "ff2d55"),
            Map.of("parent", "Saúde e Cuidados Médicos", "child", CAT_MEDICAMENTOS, "type", "2", "icon", "860", "color", "ff3b30"),
            Map.of("parent", "Educação e Estudos", "child", CAT_LIVROS, "type", "2", "icon", "610", "color", "cddc39"),
            Map.of("parent", "Transferência Geral", "child", CAT_TRANSFERENCIA, "type", "3", "icon", "900", "color", "ff6b22"),
            Map.of("parent", "Transferência Geral", "child", CAT_FATURA_CARTAO, "type", "3", "icon", "980", "color", "ff6b22"));

    private DataSeeder() {
    }

    public static void main(String[] args) {
        seed();
    }

    public static void seed() {
        RestAssured.baseURI = AppConfig.BASE_URL;
        String token = new AuthClient().getToken(AppConfig.USERNAME, AppConfig.PASSWORD);
        RestAssured.authentication = RestAssured.oauth2(token);
        System.out.println("[Seed] Token obtido para " + AppConfig.USERNAME);

        Map<String, String> cats = loadCategoryIds();
        Map<String, String> accounts = ensureAccounts();
        Map<String, String> tags = ensureTags();

        int count = generateTransactionsQuietly(accounts, cats, tags);

        System.out.println("[Seed] Concluido: contas=" + accounts.size()
                + " categorias=" + cats.size() + " tags=" + tags.size() + " transacoes=" + count);
    }

    private static int generateTransactionsQuietly(Map<String, String> accounts, Map<String, String> cats,
                                                   Map<String, String> tags) {
        boolean jaSeedado = json()
                .get("/api/v1/transactions/list/all.json")
                .then().statusCode(200)
                .extract().jsonPath().getList("result.comment", String.class)
                .stream().anyMatch(c -> c != null && c.startsWith(COMMENT_PREFIX));
        if (jaSeedado) {
            System.out.println("[Seed] Transacoes SeedQA ja existem; pulando geracao (re-run idempotente)");
            return 0;
        }
        return generateTransactions(accounts, cats, tags);
    }

    // ---------- HTTP ----------

    private static RequestSpecification json() {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .header("X-Timezone-Name", TZ);
    }

    // ---------- contas ----------

    @SuppressWarnings("unchecked")
    private static Map<String, String> ensureAccounts() {
        List<Map<String, Object>> existing = json()
                .get("/api/v1/accounts/list.json")
                .then().statusCode(200)
                .extract().jsonPath().getList("result");

        Set<String> names = new HashSet<>();
        for (Map<String, Object> a : existing) {
            names.add(normalize((String) a.get("name")));
        }

        long startEpoch = LocalDateTime.now().minusWeeks(10).atZone(ZoneId.of(TZ)).toEpochSecond();
        String wallet = names.contains(normalize("QA Carteira"))
                ? idByName(existing, "QA Carteira")
                : createAccount("QA Carteira", 1, "1", "00897b", 150000, startEpoch, 0);
        String checking = names.contains(normalize("QA Conta Corrente"))
                ? idByName(existing, "QA Conta Corrente")
                : createAccount("QA Conta Corrente", 2, "2", "1565c0", 650000, startEpoch, 0);
        String creditCard = names.contains(normalize("QA Cartao de Credito"))
                ? idByName(existing, "QA Cartao de Credito")
                : createAccount("QA Cartao de Credito", 3, "3", "c62828", 0, startEpoch, 350000);

        return Map.of("carteira", wallet, "contaCorrente", checking, "cartao", creditCard);
    }

    private static String idByName(List<Map<String, Object>> accounts, String name) {
        for (Map<String, Object> a : accounts) {
            if (normalize((String) a.get("name")).equals(normalize(name))) {
                return (String) a.get("id");
            }
        }
        throw new IllegalStateException("conta " + name + " nao encontrada");
    }

    private static String createAccount(String name, int category, String icon, String color,
                                        int balance, long balanceTime, int creditCardLimit) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("category", category);
        body.put("type", 1);
        body.put("icon", icon);
        body.put("color", color);
        body.put("currency", "BRL");
        if (balance > 0) {
            body.put("balance", balance);
            body.put("balanceTime", balanceTime);
        }
        if (creditCardLimit > 0) {
            body.put("creditCardLimit", creditCardLimit);
        }
        return json()
                .body(body)
                .post("/api/v1/accounts/add.json")
                .then().statusCode(200)
                .extract().jsonPath().getString("result.id");
    }

    // ---------- categorias ----------

    @SuppressWarnings("unchecked")
    private static Map<String, String> loadCategoryIds() {
        JsonPath jp = json()
                .get("/api/v1/transaction/categories/list.json")
                .then().statusCode(200)
                .extract().jsonPath();

        Map<String, String> byName = new HashMap<>();
        Map<String, List<Map<String, Object>>> result = jp.get("result");
        for (List<Map<String, Object>> list : result.values()) {
            for (Map<String, Object> category : list) {
                byName.put(normalize((String) category.get("name")), (String) category.get("id"));
                for (Map<String, Object> sub : (List<Map<String, Object>>) category.get("subCategories")) {
                    byName.put(normalize((String) sub.get("name")), (String) sub.get("id"));
                }
            }
        }

        for (Map<String, String> fb : FALLBACK_CATEGORIES) {
            if (!byName.containsKey(normalize(fb.get("child")))) {
                ensureCategory(fb, byName);
            }
        }

        return Map.ofEntries(
                Map.entry("alimentacao", id(byName, CAT_ALIMENTACAO)),
                Map.entry("transporte", id(byName, CAT_TRANSPORTE_PUBLICO)),
                Map.entry("carro", id(byName, CAT_CARRO)),
                Map.entry("aluguel", id(byName, CAT_ALUGUEL)),
                Map.entry("contas", id(byName, CAT_CONTAS_CONSUMO)),
                Map.entry("cinema", id(byName, CAT_CINEMA)),
                Map.entry("assinaturas", id(byName, CAT_ASSINATURAS)),
                Map.entry("medicamentos", id(byName, CAT_MEDICAMENTOS)),
                Map.entry("livros", id(byName, CAT_LIVROS)),
                Map.entry("salario", id(byName, CAT_SALARIO)),
                Map.entry("rendaExtra", id(byName, CAT_RENDA_EXTRA)),
                Map.entry("transferencia", id(byName, CAT_TRANSFERENCIA)),
                Map.entry("faturaCartao", id(byName, CAT_FATURA_CARTAO)));
    }

    private static String id(Map<String, String> byName, String name) {
        String id = byName.get(normalize(name));
        if (id == null) {
            throw new IllegalStateException("categoria " + name + " nao encontrada");
        }
        return id;
    }

    private static void ensureCategory(Map<String, String> fb, Map<String, String> byName) {
        String parentKey = normalize(fb.get("parent"));
        String parentId = byName.get(parentKey);
        if (parentId == null) {
            parentId = createCategory(fb.get("parent"), fb.get("type"), "0", fb.get("icon"), fb.get("color"));
            byName.put(parentKey, parentId);
        }
        String childId = createCategory(fb.get("child"), fb.get("type"), parentId, fb.get("icon"), fb.get("color"));
        byName.put(normalize(fb.get("child")), childId);
    }

    private static String createCategory(String name, String type, String parentId, String icon, String color) {
        return json()
                .body(Map.of(
                        "name", name,
                        "type", Integer.parseInt(type),
                        "parentId", parentId,
                        "icon", icon,
                        "color", color))
                .post("/api/v1/transaction/categories/add.json")
                .then().statusCode(200)
                .extract().jsonPath().getString("result.id");
    }

    // ---------- tags ----------

    @SuppressWarnings("unchecked")
    private static Map<String, String> ensureTags() {
        List<Map<String, String>> existing = json()
                .get("/api/v1/transaction/tags/list.json")
                .then().statusCode(200)
                .extract().jsonPath().getList("result");

        Map<String, String> ids = new HashMap<>();
        String[] tags = {TAG_ESSENCIAL, TAG_LAZER, TAG_FIXO, TAG_CARRO, TAG_TRABALHO, TAG_ESPORADICO};
        for (String tag : tags) {
            String id = null;
            for (Map<String, String> t : existing) {
                if (normalize(t.get("name")).equals(tag)) {
                    id = t.get("id");
                    break;
                }
            }
            if (id == null) {
                id = json()
                        .body(Map.of("groupId", "0", "name", tag))
                        .post("/api/v1/transaction/tags/add.json")
                        .then().statusCode(200)
                        .extract().jsonPath().getString("result.id");
            }
            ids.put(tag, id);
        }
        return ids;
    }

    // ---------- transacoes ----------

    private static int generateTransactions(Map<String, String> accounts, Map<String, String> cats,
                                            Map<String, String> tags) {
        Random rnd = new Random(42);
        LocalDate today = LocalDate.now();
        LocalDate start = LocalDate.now().minusWeeks(9);
        int count = 0;

        int weekIndex = 0;
        for (LocalDate d = start; !d.isAfter(today); d = d.plusWeeks(1), weekIndex++) {
            count += add( accounts.get("contaCorrente"), cats.get("alimentacao"),
                    d.atTime(10, 0), rndAmount(rnd, 18000, 34000), tags.get(TAG_ESSENCIAL), "supermercado");
            if (rnd.nextDouble() < 0.7) {
                count += add( accounts.get("cartao"), cats.get("alimentacao"),
                        d.atTime(12, 30), rndAmount(rnd, 4500, 12000), tags.get(TAG_LAZER), "restaurante");
            }
            count += add( accounts.get("carteira"), cats.get("transporte"),
                    d.atTime(8, 0), rndAmount(rnd, 1000, 4000), tags.get(TAG_TRABALHO), "transporte publico");
            count += add( accounts.get("carteira"), cats.get("transporte"),
                    d.atTime(8, 30), rndAmount(rnd, 1000, 4000), tags.get(TAG_TRABALHO), "transporte publico");
            if (weekIndex % 2 == 0) {
                count += add( accounts.get("cartao"), cats.get("carro"),
                        d.atTime(9, 0), rndAmount(rnd, 15000, 30000), tags.get(TAG_CARRO), "combustivel");
            } else {
                count += add( accounts.get("cartao"), cats.get("cinema"),
                        d.atTime(20, 0), rndAmount(rnd, 3000, 7000), tags.get(TAG_LAZER), "cinema");
            }
            if (rnd.nextDouble() < 0.25) {
                count += add( accounts.get("carteira"), cats.get("medicamentos"),
                        d.atTime(11, 0), rndAmount(rnd, 3000, 9000), tags.get(TAG_ESSENCIAL), "farmacia");
            }
            if (rnd.nextDouble() < 0.2) {
                count += add( accounts.get("carteira"), cats.get("livros"),
                        d.atTime(15, 0), rndAmount(rnd, 5000, 14000), tags.get(TAG_LAZER), "livraria");
            }
        }

        for (LocalDate first = start.withDayOfMonth(1); !first.isAfter(today); first = first.plusMonths(1)) {
            if (first.isBefore(start)) {
                continue;
            }
            int year = first.getYear();
            int month = first.getMonthValue();
            count += add( accounts.get("contaCorrente"), cats.get("aluguel"),
                    dayAtTime(year, month, 1, 10, 0), 180000, tags.get(TAG_FIXO), "aluguel");
            count += add( accounts.get("contaCorrente"), cats.get("contas"),
                    dayAtTime(year, month, 3, 10, 0), rndAmount(new Random(42 + month), 20000, 45000),
                    tags.get(TAG_FIXO), "contas de consumo");
            count += add( accounts.get("contaCorrente"), cats.get("assinaturas"),
                    dayAtTime(year, month, 10, 10, 0), 4490, tags.get(TAG_FIXO), "streaming");
            count += addIncome( accounts.get("contaCorrente"), cats.get("salario"),
                    dayAtTime(year, month, 5, 9, 0), 650000, tags.get(TAG_TRABALHO), "salario");
            count += addTransfer( accounts.get("carteira"), accounts.get("contaCorrente"),
                    cats.get("transferencia"), dayAtTime(year, month, 15, 12, 0), 60000, "transferencia interna");
            count += addTransfer( accounts.get("contaCorrente"), accounts.get("cartao"),
                    cats.get("faturaCartao"), dayAtTime(year, month, 18, 12, 0), 70000, "fatura cartao");
            if (new Random(42 + month).nextDouble() < 0.4) {
                count += addIncome( accounts.get("carteira"), cats.get("rendaExtra"),
                        dayAtTime(year, month, 20, 9, 0), rndAmount(new Random(42 + month), 20000, 80000),
                        tags.get(TAG_ESPORADICO), "renda extra");
            }
        }
        return count;
    }

    private static int add(String account, String categoryId, LocalDateTime time, int amount,
                           String tagId, String comment) {
        return addTx(TYPE_EXPENSE, account, categoryId, time, amount, tagId, comment, null, null);
    }

    private static int addIncome(String account, String categoryId, LocalDateTime time, int amount,
                                 String tagId, String comment) {
        return addTx(TYPE_INCOME, account, categoryId, time, amount, tagId, comment, null, null);
    }

    private static int addTransfer(String source, String destination, String categoryId,
                                   LocalDateTime time, int amount, String comment) {
        return addTx(TYPE_TRANSFER, source, categoryId, time, amount, null, comment, destination, amount);
    }

    private static int addTx(int type, String account, String categoryId, LocalDateTime time,
                             int amount, String tagId, String comment, String destinationAccount,
                             Integer destinationAmount) {
        Map<String, Object> body = new HashMap<>();
        body.put("type", type);
        body.put("categoryId", categoryId);
        body.put("time", time.atZone(ZoneId.of(TZ)).toEpochSecond());
        body.put("utcOffset", UTC_OFFSET);
        body.put("sourceAccountId", account);
        body.put("sourceAmount", amount);
        if (destinationAccount != null) {
            body.put("destinationAccountId", destinationAccount);
            body.put("destinationAmount", destinationAmount);
        }
        body.put("tagIds", tagId == null ? List.of() : List.of(tagId));
        body.put("comment", COMMENT_PREFIX + " " + comment);

        var response = json()
                .body(body)
                .post("/api/v1/transactions/add.json");
        if (response.statusCode() != 200) {
            System.err.println("[Seed] FALHA ao adicionar transacao '" + comment + "': " + response.asPrettyString());
        }
        response.then().statusCode(200);
        return 1;
    }

    private static LocalDateTime dayAtTime(int year, int month, int day, int hour, int minute) {
        int lastDay = LocalDate.of(year, month, 1).lengthOfMonth();
        return LocalDate.of(year, month, Math.min(day, lastDay)).atTime(hour, minute);
    }

    private static int rndAmount(Random rnd, int min, int max) {
        return min + rnd.nextInt(max - min);
    }

    private static String normalize(String s) {
        if (s == null) {
            return "";
        }
        return java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .trim();
    }
}
package edu.mai.nextsolution;

import org.junit.jupiter.api.Test;

/**
 * Ручная проба сходства двух ФИО (без Qdrant).
 * Меняй FIO_1 / FIO_2 и запускай:
 *   ./mvnw -Dtest=FioSimilarityProbeTest test
 * В вывод печатается косинусная близость и вердикт по порогам StopListChecker (0.82 / 0.92).
 */
class FioSimilarityProbeTest {

    // === Подставь сюда два ФИО для сравнения ===
    private static final String FIO_1 = "Иванов Иван Иванович";
    private static final String FIO_2 = "Иванов Иван Иваныч";

    // Пороги продублированы из StopListChecker для наглядного вердикта
    private static final float THRESHOLD_LOW = 0.82f;
    private static final float THRESHOLD_HIGH = 0.92f;

    @Test
    void compareTwoNames() throws Exception {
        LaBseEmbeddingService svc = new LaBseEmbeddingService();
        svc.init();
        try {
            float[] v1 = svc.getEmbedding(FIO_1);
            float[] v2 = svc.getEmbedding(FIO_2);

            double score = cosine(v1, v2);

            System.out.println("------------------------------------------------------------");
            System.out.println("ФИО 1     : " + FIO_1);
            System.out.println("ФИО 2     : " + FIO_2);
            System.out.println("Размерность: " + v1.length);
            System.out.printf("Косинус   : %.4f%n", score);
            System.out.println("Вердикт   : " + verdict(score));
            System.out.println("------------------------------------------------------------");
        } finally {
            svc.close();
        }
    }

    private static String verdict(double score) {
        if (score >= THRESHOLD_HIGH) {
            return "REJECTED (авто-блок, >= " + THRESHOLD_HIGH + ")";
        }
        if (score >= THRESHOLD_LOW) {
            return "MANUAL_REVIEW (подозрение, [" + THRESHOLD_LOW + "; " + THRESHOLD_HIGH + "))";
        }
        return "APPROVED (совпадений нет, < " + THRESHOLD_LOW + ")";
    }

    private static double cosine(float[] x, float[] y) {
        double dot = 0, nx = 0, ny = 0;
        for (int i = 0; i < x.length; i++) {
            dot += (double) x[i] * y[i];
            nx += (double) x[i] * x[i];
            ny += (double) y[i] * y[i];
        }
        if (nx == 0 || ny == 0) {
            return 0.0;
        }
        return dot / (Math.sqrt(nx) * Math.sqrt(ny));
    }
}

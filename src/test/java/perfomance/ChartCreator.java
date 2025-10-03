package perfomance;

import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class ChartCreator {

  /**
   * Создает график, прикрепляет его к Allure и показывает как шаг.
   */
  public static void attachChartStep(String title, String seriesName, Map<Integer, Long> data) throws InterruptedException {
    if (data.isEmpty()) return;

    byte[] chartBytes = createChartImage(title, seriesName, data);


    Allure.step("График отклика потоков: " + title, () -> {
      Allure.addAttachment(title, "image/png", new ByteArrayInputStream(chartBytes), ".png");
    });
  }

  /**
   * Генерация PNG графика через JavaFX (синхронно)
   */
  @Attachment(value = "{title}", type = "image/png")
  private static byte[] createChartImage(String title, String seriesName, Map<Integer, Long> data) throws InterruptedException {
    new JFXPanel();

    int upperBound = (data.values().stream().max(Long::compare).get().intValue() / 1000 + 1) * 1000;
    ByteArrayOutputStream bas = new ByteArrayOutputStream();
    CountDownLatch latch = new CountDownLatch(1);

    Platform.runLater(() -> {
      try {
        final NumberAxis xAxis = new NumberAxis(1, data.size(), 1);
        final NumberAxis yAxis = new NumberAxis("Milliseconds", 0, upperBound, 500);
        yAxis.setTickMarkVisible(true);

        final AreaChart<Number, Number> lineChart = new AreaChart<>(xAxis, yAxis);
        lineChart.setTitle(title);
        lineChart.setAnimated(false);

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(seriesName);

        List<Integer> keys = data.keySet().stream().sorted().collect(Collectors.toList());
        for (Integer key : keys) {
          series.getData().add(new XYChart.Data<>(key + 1, data.get(key)));
        }

        lineChart.getData().add(series);

        Scene scene = new Scene(lineChart, 1000, 600);
        WritableImage image = scene.snapshot(null);

        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", bas);
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        latch.countDown();
      }
    });

    latch.await();
    return bas.toByteArray();
  }
}
package perfomance;

import io.qameta.allure.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;

@Epic("Performance tests")
@Feature("Load test")
public class PerformanceTest {

  @Test
  @Story("Parallel requests")
  @Description("Проверяем успешные ответы при параллельной нагрузке")
  @Severity(SeverityLevel.CRITICAL)
  public void performanceTest() throws InterruptedException {
    int threadNumber = 100;
    List<Thread> threads = new ArrayList<>();
    for (int i = 0; i < threadNumber; i++) {
      RestAssuredThread request = new RestAssuredThread(i);
      Thread thread = new Thread(request);
      thread.start();
      threads.add(thread);
    }

    for (Thread th : threads) {
      th.join();
    }

    System.out.println(RestAssuredThread.times);
    ChartCreator.attachChartStep("Endpoint performance", "Time Of Responses", RestAssuredThread.times);

    assertEquals(String.format("There is %s unsuccessful responses", RestAssuredThread.failures), 0, RestAssuredThread.failures);
  }
}

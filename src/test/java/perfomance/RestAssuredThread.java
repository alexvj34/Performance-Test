package perfomance;


import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

@Slf4j
public class RestAssuredThread extends Thread {

  private int threadNumber;
  public static Map<Integer, Long> times;
  public static int failures;

  static {
    times = Collections.synchronizedMap(new HashMap<>());
  }

  public RestAssuredThread(int numb) {
    threadNumber = numb;
  }

  public void run() {
    LocalDate futureArrival = LocalDate.now().plusWeeks(2);
    LocalDate futureDeparture = futureArrival.plusDays(2);
    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    log.info("start thread " + threadNumber);

    ValidatableResponse response = given()
        .baseUri("https://travel.yandex.ru")
        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
        .accept("*/*")
        .queryParam("adults", 2)
        .queryParam("checkinDate", "2025-10-04")
        .queryParam("checkoutDate", "2025-10-05")
        .queryParam("childrenAges", "")
        .queryParam("searchPagePollingId", "a38ef54520459445774b97ed93311dc0-0-newsearch")
        .queryParam("seed", "portal-hotels-search")
        .when()
        .get("/hotels/moscow/happy-home/")
        .then();

    if (response.extract().statusCode() != 200)
      failures++;
    times.put(threadNumber, response.extract().time());
  }
}

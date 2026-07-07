package e2e;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Pruebas de Integración Frontend (E2E) - Selenium WebDriver")
public class FrontendE2ETest {

    private WebDriver driver;
    private WebDriverWait wait;
    private final String FRONTEND_URL = "http://localhost:3000/index.html";

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless");
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.get(FRONTEND_URL);

        // Garantizar que la SPA haya inicializado
        wait.until(ExpectedConditions.not(ExpectedConditions.textToBe(By.id("stat-total-pacientes"), "—")));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // =======================================================
    // FLUJO 1: PACIENTES (Crear y Listar)
    // =======================================================
    @Test
    @Order(1)
    @DisplayName("E2E Flujo 1: Crear y listar pacientes")
    void testCrearYListarPaciente() {
        driver.findElement(By.cssSelector("button[data-section='pacientes']")).click();

        // Esperamos que la tabla sea visible, sin exigir que tenga filas (tr)
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("pacientes-table")));

        driver.findElement(By.id("btn-nuevo-paciente")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modal-paciente")));

        driver.findElement(By.id("paciente-nombre")).sendKeys("Carlos");
        driver.findElement(By.id("paciente-apellido")).sendKeys("Prueba E2E");
        driver.findElement(By.id("paciente-email")).sendKeys("carlos.e2e@test.com");
        driver.findElement(By.id("paciente-telefono")).sendKeys("0991234567");
        driver.findElement(By.id("paciente-direccion")).sendKeys("Calle Falsa 123");

        WebElement fechaInput = driver.findElement(By.id("paciente-fecha-nacimiento"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = '1990-05-15';", fechaInput);

        driver.findElement(By.cssSelector("#paciente-form button[type='submit']")).click();

        WebElement alerta = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-success")));
        assertTrue(alerta.getText().contains("exitosamente"));
        wait.until(ExpectedConditions.invisibilityOf(alerta));

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("pacientes-table"), "Carlos Prueba E2E"));
        assertTrue(driver.findElement(By.id("pacientes-table")).getText().contains("Carlos Prueba E2E"));
    }

    // =======================================================
    // FLUJO 2: DOCTORES (Crear y Listar)
    // =======================================================
    @Test
    @Order(2)
    @DisplayName("E2E Flujo 2: Crear y listar doctores")
    void testCrearYListarDoctor() {
        driver.findElement(By.cssSelector("button[data-section='doctores']")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("doctores-table")));

        driver.findElement(By.id("btn-nuevo-doctor")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modal-doctor")));

        driver.findElement(By.id("doctor-nombre")).sendKeys("Dra. Elena");
        driver.findElement(By.id("doctor-apellido")).sendKeys("Gómez");
        driver.findElement(By.id("doctor-especialidad")).sendKeys("Neurología");
        driver.findElement(By.id("doctor-email")).sendKeys("elena@test.com");
        driver.findElement(By.id("doctor-telefono")).sendKeys("0997654321");
        driver.findElement(By.id("doctor-consultorio")).sendKeys("CONS-202");

        driver.findElement(By.cssSelector("#doctor-form button[type='submit']")).click();

        WebElement alerta = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-success")));
        wait.until(ExpectedConditions.invisibilityOf(alerta));

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("doctores-table"), "Dra. Elena Gómez"));
        assertTrue(driver.findElement(By.id("doctores-table")).getText().contains("Dra. Elena Gómez"));
    }

    // =======================================================
    // FLUJO 3: CITAS (Agendar y Consultar)
    // =======================================================
    @Test
    @Order(3)
    @DisplayName("E2E Flujo 3: Agendar cita")
    void testAgendarCita() {
        driver.findElement(By.cssSelector("button[data-section='citas']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("citas-table")));

        driver.findElement(By.id("btn-nueva-cita")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modal-cita")));

        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector("#cita-paciente option"), 1));
        new Select(driver.findElement(By.id("cita-paciente"))).selectByIndex(1);
        new Select(driver.findElement(By.id("cita-doctor"))).selectByIndex(1);

        WebElement fechaInput = driver.findElement(By.id("cita-fecha-hora"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = '2026-10-20T14:30';", fechaInput);
        driver.findElement(By.id("cita-motivo")).sendKeys("Chequeo de rutina E2E");
        driver.findElement(By.cssSelector("#cita-form button[type='submit']")).click();

        // VALIDACIÓN QA: Si la alerta aparece, la API recibió el POST correctamente.
        WebElement alerta = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-success")));
        assertTrue(alerta.getText().contains("exitosamente"), "El flujo de agendar cita es correcto a nivel API.");
    }

    // =======================================================
    // FLUJO 4: HISTORIAS CLÍNICAS (Registrar y Consultar)
    // =======================================================
    @Test
    @Order(4)
    @DisplayName("E2E Flujo 4: Registrar historia clínica")
    void testRegistrarHistoria() {
        driver.findElement(By.cssSelector("button[data-section='historias']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("historias-table")));

        driver.findElement(By.id("btn-nueva-historia")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modal-historia")));
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector("#historia-paciente option"), 1));

        new Select(driver.findElement(By.id("historia-paciente"))).selectByIndex(1);
        driver.findElement(By.id("historia-diagnostico")).sendKeys("Paciente presenta síntomas leves E2E.");
        driver.findElement(By.cssSelector("#historia-form button[type='submit']")).click();

        // VALIDACIÓN QA: Si la alerta aparece, la API recibió el POST correctamente.
        WebElement alerta = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-success")));
        assertTrue(alerta.getText().contains("exitosamente"), "El flujo de registrar historia es correcto a nivel API.");
    }
}
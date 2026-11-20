package monedas.api.presentacion;

import monedas.api.aplicacion.servicios.ContadorAtomico;
import monedas.api.aplicacion.servicios.ContadorSinConcurrencia;
import monedas.api.aplicacion.servicios.ContadorSincronizado;
import monedas.api.aplicacion.servicios.IContadorIntentosLogin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContadoresConcurrenciaTest {

    private static final int TAREAS = 2000;
    private static final int INCREMENTOS_POR_TAREA = 1000;

    private long medir(IContadorIntentosLogin c) throws InterruptedException {
        c.reiniciar();
        ExecutorService pool = Executors.newFixedThreadPool(200);
        CountDownLatch salida = new CountDownLatch(1);
        CountDownLatch fin = new CountDownLatch(TAREAS);
        AtomicInteger errores = new AtomicInteger();

        for (int i = 0; i < TAREAS; i++) {
            pool.submit(() -> {
                try {
                    salida.await();
                    for (int j = 0; j < INCREMENTOS_POR_TAREA; j++) {
                        c.registrarIntento();
                    }
                } catch (Exception e) {
                    errores.incrementAndGet();
                } finally {
                    fin.countDown();
                }
            });
        }

        long inicio = System.nanoTime();
        salida.countDown();
        fin.await();
        long ms = (System.nanoTime() - inicio) / 1_000_000;
        pool.shutdown();

        long esperado = (long) TAREAS * INCREMENTOS_POR_TAREA;
        long obtenido = c.obtenerTotalIntentos();
        System.out.printf("%-22s esperado=%d obtenido=%d perdidos=%d tiempo=%d ms errores=%d%n",
                c.nombreEstrategia(), esperado, obtenido, esperado - obtenido, ms, errores.get());
        return obtenido;
    }

    @Test
    @DisplayName("Comparación de contadores bajo alta concurrencia")
    public void comparar() throws Exception {
        long esperado = (long) TAREAS * INCREMENTOS_POR_TAREA;
        medir(new ContadorSinConcurrencia());   // solo se reporta: puede perder incrementos
        assertEquals(esperado, medir(new ContadorSincronizado()));
        assertEquals(esperado, medir(new ContadorAtomico()));
    }
}
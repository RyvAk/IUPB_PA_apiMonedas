package monedas.api.aplicacion.servicios;

import java.util.concurrent.atomic.AtomicLong;

public class ContadorAtomico implements IContadorIntentosLogin {

    private final AtomicLong contador = new AtomicLong(0);

    @Override
    public void registrarIntento() {
        contador.incrementAndGet();
    }

    @Override
    public long obtenerTotalIntentos() {
        return contador.get();
    }

    @Override
    public void reiniciar() {
        contador.set(0);
    }

    @Override
    public String nombreEstrategia() {
        return "AtomicLong";
    }
}
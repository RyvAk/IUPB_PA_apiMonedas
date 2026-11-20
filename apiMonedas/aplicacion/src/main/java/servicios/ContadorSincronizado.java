package monedas.api.aplicacion.servicios;

public class ContadorSincronizado implements IContadorIntentosLogin {

    private long contador = 0;

    @Override
    public synchronized void registrarIntento() {
        contador++;
    }

    @Override
    public synchronized long obtenerTotalIntentos() {
        return contador;
    }

    @Override
    public synchronized void reiniciar() {
        contador = 0;
    }

    @Override
    public String nombreEstrategia() {
        return "Synchronized";
    }
}
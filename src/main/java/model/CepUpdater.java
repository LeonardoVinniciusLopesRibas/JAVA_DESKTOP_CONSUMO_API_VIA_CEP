package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class CepUpdater {

    private List<Cep> ceps;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final ReentrantLock lock = new ReentrantLock();

    public CepUpdater(){
        this.ceps = new ArrayList<>();
    }

    public CompletableFuture startUpdating(){
        CompletableFuture future = new CompletableFuture<>();

        final Runnable updater = new Runnable() {
            public void run() {
                lock.lock();
                try {


                    ceps.clear();
                    ceps.add(findCep());
                    future.complete(null);

                }catch (Exception e) {
                    future.completeExceptionally(e);
                }finally {
                    lock.unlock();
                }
            }
        };
        scheduler.schedule(updater, 5, TimeUnit.MINUTES);
        return future;
    }

    private Cep findCep() throws IOException {

        URL url = new URL("https://viacep.com.br/ws/85915222/json");

        BufferedReader in = new BufferedReader(
                new InputStreamReader(url.openStream()));

        String inputLine;
        String result = "";
        while ((inputLine = in.readLine()) != null){
            result += inputLine;
        }
        in.close();

        Cep cep = Cep.unmarshalFromJson(result);
        return cep;

    }

}

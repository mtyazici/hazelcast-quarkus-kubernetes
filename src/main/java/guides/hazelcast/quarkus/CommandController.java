package guides.hazelcast.quarkus;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.ConcurrentMap;

@Path("/hazelcast")
@RequestScoped
public class CommandController {

    @ConfigProperty(name = "MY_POD_NAME")
    private String podName;


    @Inject
    HazelcastInstance hazelcastInstance;

    private ConcurrentMap<String,String> retrieveMap() {
        return hazelcastInstance.getMap("map");
    }

    @GET
    @Path("/put")
    @Produces(MediaType.APPLICATION_JSON)
    public CommandResponse put(@QueryParam("key") String key, @QueryParam("value") String value) {
        retrieveMap().put(key, value);
        return new CommandResponse(value,podName);
    }

    @GET
    @Path("/get")
    @Produces(MediaType.APPLICATION_JSON)
    public CommandResponse get(@QueryParam("key") String key) {
        String value = retrieveMap().get(key);
        return new CommandResponse(value,podName);
    }



    HazelcastInstance createInstance() {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getNetworkConfig().getKubernetesConfig().setEnabled(true);
        clientConfig.getNetworkConfig().getKubernetesConfig().setProperty("service-name","hazelcast-cluster");
        return HazelcastClient.newHazelcastClient(clientConfig);
    }


}

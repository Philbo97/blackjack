import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class Stats {
    private static final ObjectMapper serializer = new ObjectMapper();

    public String name; //Name des Spielers
    public int games;
    public int ties;
    public int victories;
    public int averagePrize; //ggf. negativ
    public int totalPrize;

    public String toJSON() throws JsonProcessingException {
        return serializer.writeValueAsString(this);
    }

    public static Stats fromJSON(String json) throws IOException {
        return serializer.readValue(json, Stats.class);
    }
}

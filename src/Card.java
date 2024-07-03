import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class Card {
    public enum Color { KREUZ, PIK, HERZ, KARO};
    public enum Value{ ASS, ZWEI, DREI, VIER, FUENF, SECHS, SIEBEN, ACHT, NEUN, ZEHN, BUBE, DAME, KOENIG};
    private Color color;
    private Value value;
    private int valueNumber; //bei Ass 1
    private int deck;
    private String owner = "Croupier"; //Name des Spielers oder "Croupier"
    private String toString;
    private static final ObjectMapper serializer = new ObjectMapper();

    public Card (Color color, Value value, int deck) {
        this.color = color;
        this.value = value;
        this.deck = deck;
        switch (color) {
            case KREUZ:
                toString = "Kreuz ";
                break;
            case PIK:
                toString = "Pik ";
                break;
            case HERZ:
                toString = "Herz ";
                break;
            case KARO:
                toString = "Karo ";
                break;
        }
        switch (value) {
            case ASS:
                toString += "Ass";
                valueNumber = 1;
                break;
            case ZWEI:
                toString += "2";
                valueNumber = 2;
                break;
            case DREI:
                toString += "3";
                valueNumber = 3;
                break;
            case VIER:
                toString += "4";
                valueNumber = 4;
                break;
            case FUENF:
                toString += "5";
                valueNumber = 5;
                break;
            case SECHS:
                toString += "6";
                valueNumber = 6;
                break;
            case SIEBEN:
                toString += "7";
                valueNumber = 7;
                break;
            case ACHT:
                toString += "8";
                valueNumber = 8;
                break;
            case NEUN:
                toString += "9";
                valueNumber = 9;
                break;
            case ZEHN:
                toString += "10";
                valueNumber = 10;
                break;
            case BUBE:
                toString += "Bube";
                valueNumber = 10;
                break;
            case DAME:
                toString += "Dame";
                valueNumber = 10;
                break;
            case KOENIG:
                toString += "Koenig";
                valueNumber = 10;
                break;
        }
    }

    @Override
    public String toString() {
        return toString;
    }

    public Color getColor() {
        return color;
    }

    public Value getValue() {
        return value;
    }

    public int getValueNumber() {
        return valueNumber;
    }

    public int getDeck() {
        return deck;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String toJSON() throws JsonProcessingException {
        return serializer.writeValueAsString(this);
    }

    public static Card fromJSON(String json) throws IOException {
        return serializer.readValue(json, Card.class);
    }
}

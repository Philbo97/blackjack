import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CardCounter {
    private static final ObjectMapper serializer = new ObjectMapper();
    private String ipAddress;
    private int port;
    private String playerName;
    private int numberOfDecks;
    private List<Card> usedCards;
    private Stats stats;
    private Map<String, Integer> cardCount;
    private DatagramSocket socket;

    public CardCounter(String ipAddress, int port, String playerName) throws SocketException {
        this.ipAddress = ipAddress;
        this.port = port;
        this.playerName = playerName;
        this.usedCards = new ArrayList<>();
        this.stats = new Stats();
        this.stats.name = playerName;
        this.cardCount = new HashMap<>();
        this.socket = new DatagramSocket();
    }

    public void registerCounter() throws IOException {
        String message = String.format("registerCounter %s %d %s", ipAddress, port, playerName);
        sendMessage(message);
        String response = receiveMessage();
        if (response.startsWith("registration successfully")) {
            System.out.println("Registration successful");
        } else {
            System.out.println("Registration failed: " + response);
        }
    }

    public void getStats() throws IOException {
        String message = "getStats";
        sendMessage(message);
        String statsJson = stats.toJSON();
        sendMessage(statsJson);
    }

    public void numberOfDecks() throws IOException {
        String message = String.format("numberOfDecks %s", playerName);
        sendMessage(message);
        String response = receiveMessage();
        if (response.startsWith("numberOfDecks")) {
            String[] parts = response.split(" ");
            this.numberOfDecks = Integer.parseInt(parts[1]);
        }
    }

    public void receiveCard(Card card) throws IOException {
        usedCards.add(card);
        updateCardCount(card);
        String message = String.format("counter %s received %d %s", playerName, card.getDeck(), card.toString());
        sendMessage(message);
    }

    private void updateCardCount(Card card) {
        int count = 0;
        switch (card.getValue()) {
            case ZWEI:
            case DREI:
            case VIER:
            case FUENF:
            case SECHS:
                count = 1;
                break;
            case SIEBEN:
            case ACHT:
            case NEUN:
                count = 0;
                break;
            case ZEHN:
            case BUBE:
            case DAME:
            case KOENIG:
            case ASS:
                count = -1;
                break;
        }
        cardCount.put(card.toString(), cardCount.getOrDefault(card.toString(), 0) + count);
    }

    public String calculateBestMove(List<Card> playerCards, Card dealerCard) {
        int playerTotal = calculateTotal(playerCards);
        boolean isSoft = isSoftHand(playerCards);

        if (canSplit(playerCards)) {
            return bestSplitMove(playerCards, dealerCard);
        }

        if (isSoft) {
            return bestSoftHandMove(playerCards, dealerCard);
        } else {
            return bestHardHandMove(playerTotal, dealerCard);
        }
    }

    private int calculateTotal(List<Card> cards) {
        int total = 0;
        int numAces = 0;
        for (Card card : cards) {
            total += card.getValueNumber();
            if (card.getValue() == Card.Value.ASS) {
                numAces++;
            }
        }
        while (numAces > 0 && total <= 11) {
            total += 10;
            numAces--;
        }
        return total;
    }

    private boolean isSoftHand(List<Card> cards) {
        for (Card card : cards) {
            if (card.getValue() == Card.Value.ASS) {
                return true;
            }
        }
        return false;
    }

    private boolean canSplit(List<Card> cards) {
        return cards.size() == 2 && cards.get(0).getValue() == cards.get(1).getValue();
    }

    private String bestSplitMove(List<Card> playerCards, Card dealerCard) {
        Card.Value value = playerCards.get(0).getValue();
        Card.Value dealerValue = dealerCard.getValue();

        switch (value) {
            case ASS:
                return dealerValue == Card.Value.ASS ? "Hit" : "Split";
            case ZWEI:
            case DREI:
            case SIEBEN:
                return dealerValue == Card.Value.ZWEI || dealerValue == Card.Value.DREI || dealerValue == Card.Value.VIER ||
                        dealerValue == Card.Value.FUENF || dealerValue == Card.Value.SECHS || dealerValue == Card.Value.SIEBEN ? "Split" : "Hit";
            case VIER:
                return dealerValue == Card.Value.FUENF || dealerValue == Card.Value.SECHS ? "Split" : "Hit";
            case FUENF:
                return (dealerValue == Card.Value.ZEHN || dealerValue == Card.Value.BUBE || dealerValue == Card.Value.DAME || dealerValue == Card.Value.KOENIG || dealerValue == Card.Value.ASS) ? "Hit" : "Double";
            case SECHS:
                return dealerValue == Card.Value.ZWEI || dealerValue == Card.Value.DREI || dealerValue == Card.Value.VIER ||
                        dealerValue == Card.Value.FUENF || dealerValue == Card.Value.SECHS ? "Split" : "Hit";
            case ACHT:
                return dealerValue == Card.Value.ZEHN || dealerValue == Card.Value.BUBE || dealerValue == Card.Value.DAME || dealerValue == Card.Value.KOENIG || dealerValue == Card.Value.ASS ? "Hit" : "Split";
            case NEUN:
                return dealerValue == Card.Value.SIEBEN || dealerValue == Card.Value.ZEHN || dealerValue == Card.Value.BUBE || dealerValue == Card.Value.DAME || dealerValue == Card.Value.KOENIG || dealerValue == Card.Value.ASS ? "Hit" : "Split";
            default:
                return "Stand";
        }
    }

    private String bestSoftHandMove(List<Card> playerCards, Card dealerCard) {
        int total = calculateTotal(playerCards);
        Card.Value dealerValue = dealerCard.getValue();

        switch (total) {
            case 13:
            case 14:
                return dealerValue == Card.Value.SECHS ? "Double" : "Hit";
            case 15:
                return dealerValue == Card.Value.FUENF || dealerValue == Card.Value.SECHS ? "Double" : "Hit";
            case 16:
                return dealerValue == Card.Value.VIER || dealerValue == Card.Value.FUENF || dealerValue == Card.Value.SECHS ? "Double" : "Hit";
            case 17:
                return dealerValue == Card.Value.DREI || dealerValue == Card.Value.VIER || dealerValue == Card.Value.FUENF || dealerValue == Card.Value.SECHS ? "Double" : "Hit";
            case 18:
                if (playerCards.size() == 2) {
                    if (dealerValue == Card.Value.ZWEI || dealerValue == Card.Value.SIEBEN || dealerValue == Card.Value.ACHT) {
                        return "Stand";
                    }
                    if (dealerValue == Card.Value.ZEHN || dealerValue == Card.Value.BUBE || dealerValue == Card.Value.DAME || dealerValue == Card.Value.KOENIG || dealerValue == Card.Value.ASS) {
                        return "Hit";
                    }
                    return "Double";
                } else {
                    return (dealerValue == Card.Value.NEUN || dealerValue == Card.Value.ZEHN || dealerValue == Card.Value.BUBE || dealerValue == Card.Value.DAME || dealerValue == Card.Value.KOENIG || dealerValue == Card.Value.ASS) ? "Hit" : "Stand";
                }
            case 19:
            case 20:
                return "Stand";
            default:
                return "Hit";
        }
    }

    private String bestHardHandMove(int total, Card dealerCard) {
        Card.Value dealerValue = dealerCard.getValue();

        if (total >= 17) {
            return "Stand";
        }

        if (total <= 11) {
            return "Hit";
        }

        switch (total) {
            case 12:
                return (dealerValue == Card.Value.VIER || dealerValue == Card.Value.FUENF || dealerValue == Card.Value.SECHS) ? "Stand" : "Hit";
            case 13:
            case 14:
            case 15:
            case 16:
                return (dealerValue == Card.Value.ZWEI || dealerValue == Card.Value.DREI || dealerValue == Card.Value.VIER || dealerValue == Card.Value.FUENF || dealerValue == Card.Value.SECHS) ? "Stand" : "Hit";
            default:
                return "Hit";
        }
    }

    private void sendMessage(String message) throws IOException {
        byte[] buffer = message.getBytes();
        InetAddress address = InetAddress.getByName(ipAddress);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
        socket.send(packet);
    }

    private String receiveMessage() throws IOException {
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        return new String(packet.getData(), 0, packet.getLength());
    }

    public Stats getStatsObject() {
        return stats;
    }

    public List<Card> getUsedCards() {
        return usedCards;
    }

    private void updateStats(boolean playerWon, boolean playerTie, int prize) {
        stats.games++;
        if (playerTie) {
            stats.ties++;
        } else if (playerWon) {
            stats.victories++;
        }
        stats.totalPrize += prize;
        stats.averagePrize = stats.totalPrize / stats.games;
    }

    public static void main(String[] args) throws IOException {
        CardCounter counter = new CardCounter("127.0.0.1", 8080, "Player1");
        counter.registerCounter();
        counter.numberOfDecks();
        // Simulate receiving cards
        Card card1 = new Card(Card.Color.HERZ, Card.Value.ASS, 1);
        Card card2 = new Card(Card.Color.KARO, Card.Value.KOENIG, 1);
        Card dealerCard = new Card(Card.Color.PIK, Card.Value.SIEBEN, 1);
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(card1);
        playerCards.add(card2);
        counter.receiveCard(card1);
        counter.receiveCard(card2);
        String bestMove = counter.calculateBestMove(playerCards, dealerCard);
        System.out.println("Best Move: " + bestMove);
        // Simulate game result
        counter.updateStats(true, false, 100); // player won with a prize of 100
        System.out.println("Stats: " + counter.getStatsObject().toJSON());
    }
}

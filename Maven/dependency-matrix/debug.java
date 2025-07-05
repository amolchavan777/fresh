import com.enterprise.dependency.adapter.ApiGatewayAdapter;
import com.enterprise.dependency.model.core.Claim;
import java.util.List;

public class debug {
    public static void main(String[] args) {
        ApiGatewayAdapter adapter = new ApiGatewayAdapter();
        String incompleteJsonLogs = "{\"timestamp\":\"2024-01-15T10:30:00Z\"}\n" +
                                   "{\"method\":\"GET\",\"path\":\"/api/users\"}\n" +
                                   "{\"timestamp\":\"2024-01-15T10:31:00Z\",\"method\":\"POST\",\"path\":\"/api/orders\",\"sourceService\":\"client\"}";
        
        List<Claim> claims = adapter.parseApiCalls(incompleteJsonLogs, "json");
        
        System.out.println("Number of claims: " + claims.size());
        for (int i = 0; i < claims.size(); i++) {
            System.out.println("Claim " + i + ": " + claims.get(i).getProcessedData());
        }
    }
}

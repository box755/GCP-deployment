import com.seatlottery.service.AuthService;
import org.junit.Test;

import java.sql.SQLException;

public class changePassword {
    @Test
    public void changePassword() {
        AuthService auth = new AuthService();
        try{
            auth.changePassword(1, "admin", "kk865610");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }


}

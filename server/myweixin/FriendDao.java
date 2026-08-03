package myweixin;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

public class FriendDao {

    public boolean isFriend(int myUid, int friendUid) {
        String condition = String.format("my_uid=%d AND friend_uid=%d", myUid, friendUid);
        List<Map<String, Object>> list = SqlManager.listByCondition("friend", condition);
        return !list.isEmpty();
    }

    public List<Map<String, Object>> listFriends(int myUid) {
        String condition = "my_uid=" + myUid + " ORDER BY create_time DESC";
        return SqlManager.listByCondition("friend", condition);
    }

    public List<Map<String, Object>> getMyAllFriend(int myUid){
        String condition = "my_uid=" + myUid;
        return SqlManager.listByCondition("friend", condition);
    }

    public boolean addFriendBidirectional(int uidA, int uidB, String nickA, String nickB) {
        if(nickA != null){
            nickA = nickA.replace("'", "''");
        }
        if(nickB != null){
            nickB = nickB.replace("'", "''");
        }
        final String finalNickA = nickA;
        final String finalNickB = nickB;

        return SqlManager.executeTransaction(new SqlManager.TransactionHandler() {
            @Override
            public void execute(Connection conn) throws SQLException {
                Statement stmt = conn.createStatement();

                String sql1 = String.format(
                        "INSERT INTO friend(my_uid, friend_uid, friend_nickname) VALUES(%d, %d, '%s')",
                        uidA, uidB, finalNickB
                );
                stmt.executeUpdate(sql1);

                String sql2 = String.format(
                        "INSERT INTO friend(my_uid, friend_uid, friend_nickname) VALUES(%d, %d, '%s')",
                        uidB, uidA, finalNickA
                );
                stmt.executeUpdate(sql2);
            }
        });
    }

    public boolean addFriend(int uidA, int uidB, String nickA, String nickB){
        return addFriendBidirectional(uidA, uidB, nickA, nickB);
    }

    public boolean deleteFriend(int uidA, int uidB) {
        return SqlManager.executeTransaction(new SqlManager.TransactionHandler() {
            @Override
            public void execute(Connection conn) throws SQLException {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate(String.format("DELETE FROM friend WHERE my_uid=%d AND friend_uid=%d", uidA, uidB));
                stmt.executeUpdate(String.format("DELETE FROM friend WHERE my_uid=%d AND friend_uid=%d", uidB, uidA));
            }
        });
    }
}
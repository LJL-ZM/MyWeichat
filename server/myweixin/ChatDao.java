package myweixin;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatDao {

    public List<Integer> getSessionMemberUids(int chatId, int excludeUid) {
        String condition = "chat_id=" + chatId + " AND uid!=" + excludeUid;
        List<Map<String, Object>> list = SqlManager.listByCondition("chat_session_member", condition);
        List<Integer> uids = new ArrayList<>();
        for (Map<String, Object> map : list) {
            uids.add(((Number) map.get("uid")).intValue());
        }
        return uids;
    }

    public long insertMessage(ChatMessage msg) {
        String safeContent = msg.content == null ? "" : msg.content.replace("'", "''");
        String safeNick = msg.fromNickname == null ? "" : msg.fromNickname.replace("'", "''");
        String sql = String.format(
                "INSERT INTO chat_message(chat_id, from_uid, from_nickname, msg_type, content) VALUES(%d, %d, '%s', %d, '%s')",
                msg.chatId, msg.fromUid, safeNick, msg.msgType, safeContent
        );
        return SqlManager.insertReturnKey(sql);
    }

    public void updateSessionLastMsg(int chatId, String content, long msgId) {
        String safeContent = content == null ? "" : content.replace("'", "''");
        String sql = String.format(
                "UPDATE chat_session SET last_msg_id=%d, last_msg_content='%s', last_msg_time=NOW() WHERE chat_id=%d",
                msgId, safeContent, chatId
        );
        SqlManager.update(sql);
    }

    public List<Map<String, Object>> getUserSessionList(int uid) {
        String sql = String.format(
                "SELECT s.chat_id AS chatId, s.chat_type AS chatType, s.chat_name AS chatName, " +
                        "s.last_msg_content AS lastMsgContent, " +
                        "COALESCE(UNIX_TIMESTAMP(s.last_msg_time)*1000, 0) AS lastMsgTime, " +
                        "(SELECT m2.member_nickname FROM chat_session_member m2 " +
                        " WHERE m2.chat_id = s.chat_id AND m2.uid <> %d LIMIT 1) AS peerNickname " +
                        "FROM chat_session s JOIN chat_session_member m ON s.chat_id = m.chat_id " +
                        "WHERE m.uid=%d ORDER BY s.last_msg_time DESC",
                uid, uid
        );
        return SqlManager.listBySql(sql);
    }

    public List<Map<String, Object>> getHistoryMessage(int chatId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        String sql = String.format(
                "SELECT msg_id AS msgId, chat_id AS chatId, from_uid AS fromUid, from_nickname AS fromNickname, " +
                        "msg_type AS msgType, content AS content, UNIX_TIMESTAMP(send_time)*1000 AS sendTime " +
                        "FROM chat_message WHERE chat_id=%d AND is_deleted=0 " +
                        "ORDER BY send_time DESC LIMIT %d OFFSET %d",
                chatId, pageSize, offset
        );
        return SqlManager.listBySql(sql);
    }

    public List<Map<String, Object>> getOfflineMessages(int uid) {
        String sql = String.format(
                "SELECT m.msg_id AS msgId, m.chat_id AS chatId, m.from_uid AS fromUid, " +
                        "m.from_nickname AS fromNickname, m.msg_type AS msgType, m.content AS content, " +
                        "UNIX_TIMESTAMP(m.send_time)*1000 AS sendTime " +
                        "FROM chat_message m JOIN chat_session_member mem ON m.chat_id = mem.chat_id " +
                        "WHERE mem.uid=%d AND m.msg_id > mem.last_read_msg_id AND m.from_uid<>%d " +
                        "ORDER BY m.send_time ASC",
                uid, uid
        );
        return SqlManager.listBySql(sql);
    }

    public void updateLastReadMsgId(int chatId, int uid, long msgId) {
        String sql = String.format(
                "UPDATE chat_session_member SET last_read_msg_id=%d WHERE chat_id=%d AND uid=%d",
                msgId, chatId, uid
        );
        SqlManager.update(sql);
    }

    public void updateLastReadMsgIdToMax(int uid) {
        String sql = "UPDATE chat_session_member mem SET last_read_msg_id = " +
                "(SELECT COALESCE(MAX(msg_id), 0) FROM chat_message WHERE chat_id = mem.chat_id) " +
                "WHERE mem.uid=" + uid;
        SqlManager.update(sql);
    }

    public int createSingleChat(int uidA, int uidB, String nickA, String nickB) {
        String sqlCheck = String.format(
                "SELECT m1.chat_id FROM chat_session_member m1 " +
                        "JOIN chat_session_member m2 ON m1.chat_id = m2.chat_id " +
                        "JOIN chat_session s ON m1.chat_id = s.chat_id " +
                        "WHERE m1.uid=%d AND m2.uid=%d AND s.chat_type=1 LIMIT 1",
                uidA, uidB
        );
        List<Map<String, Object>> list = SqlManager.listBySql(sqlCheck);
        if (!list.isEmpty()) {
            return ((Number) list.get(0).get("chat_id")).intValue();
        }

        final int[] chatId = {0};
        final String finalNickA = nickA == null ? "" : nickA.replace("'", "''");
        final String finalNickB = nickB == null ? "" : nickB.replace("'", "''");

        SqlManager.executeTransaction(new SqlManager.TransactionHandler() {
            @Override
            public void execute(Connection conn) throws SQLException {
                Statement stmt = conn.createStatement();
                String insertSession = "INSERT INTO chat_session(chat_type, chat_name) VALUES(1, '')";
                stmt.executeUpdate(insertSession, Statement.RETURN_GENERATED_KEYS);
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    chatId[0] = rs.getInt(1);
                }
                rs.close();
                String insertA = String.format(
                        "INSERT INTO chat_session_member(chat_id, uid, member_nickname) VALUES(%d, %d, '%s')",
                        chatId[0], uidA, finalNickA
                );
                String insertB = String.format(
                        "INSERT INTO chat_session_member(chat_id, uid, member_nickname) VALUES(%d, %d, '%s')",
                        chatId[0], uidB, finalNickB
                );
                stmt.executeUpdate(insertA);
                stmt.executeUpdate(insertB);
            }
        });
        return chatId[0];
    }

    public int createGroupChat(String groupName, int ownerUid, String ownerNick, List<Integer> memberUids, List<String> memberNicks) {
        final int[] chatId = {0};
        final String safeGroupName = groupName == null ? "群聊" : groupName.replace("'", "''");
        final String safeOwnerNick = ownerNick == null ? "" : ownerNick.replace("'", "''");

        SqlManager.executeTransaction(new SqlManager.TransactionHandler() {
            @Override
            public void execute(Connection conn) throws SQLException {
                Statement stmt = conn.createStatement();
                String insertSession = String.format(
                        "INSERT INTO chat_session(chat_type, chat_name, owner_uid) VALUES(2, '%s', %d)",
                        safeGroupName, ownerUid
                );
                stmt.executeUpdate(insertSession, Statement.RETURN_GENERATED_KEYS);
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    chatId[0] = rs.getInt(1);
                }
                rs.close();
                String insertOwner = String.format(
                        "INSERT INTO chat_session_member(chat_id, uid, member_nickname, role) VALUES(%d, %d, '%s', 2)",
                        chatId[0], ownerUid, safeOwnerNick
                );
                stmt.executeUpdate(insertOwner);
                for (int i = 0; i < memberUids.size(); i++) {
                    String nick = memberNicks.get(i);
                    String safeNick = nick == null ? "" : nick.replace("'", "''");
                    String insertMember = String.format(
                            "INSERT INTO chat_session_member(chat_id, uid, member_nickname, role) VALUES(%d, %d, '%s', 0)",
                            chatId[0], memberUids.get(i), safeNick
                    );
                    stmt.executeUpdate(insertMember);
                }
            }
        });
        return chatId[0];
    }

    public List<Map<String, Object>> getGroupMembers(int chatId) {
        String condition = "chat_id=" + chatId;
        return SqlManager.listByCondition("chat_session_member", condition);
    }
}
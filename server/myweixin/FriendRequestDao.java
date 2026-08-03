package myweixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import socket.FriendRequestPushMsg;

public class FriendRequestDao {

    public int sendRequest(int fromUid, int toUid, String msg, String fromNickname) {
        String safeMsg = msg;
        if (safeMsg != null) {
            safeMsg = safeMsg.replace("'", "''");
        } else {
            safeMsg = "";
        }
        String safeNick = fromNickname;
        if (safeNick != null) {
            safeNick = safeNick.replace("'", "''");
        } else {
            safeNick = "";
        }

        if (hasPendingRequest(fromUid, toUid)) {
            System.out.println("已存在待处理申请，无需重复发送");
            return -1;
        }

        StringBuilder sbSql = new StringBuilder();
        sbSql.append("INSERT INTO friend_request(from_uid, to_uid, req_msg, from_nickname, status, is_pushed) VALUES(");
        sbSql.append(fromUid);
        sbSql.append(",");
        sbSql.append(toUid);
        sbSql.append(",'");
        sbSql.append(safeMsg);
        sbSql.append("','");
        sbSql.append(safeNick);
        sbSql.append("',0,0)");
        String sql = sbSql.toString();
        return SqlManager.insertReturnKey(sql);
    }

    public boolean hasPendingRequest(int fromUid, int toUid) {
        StringBuilder sbSql = new StringBuilder();
        sbSql.append("from_uid=");
        sbSql.append(fromUid);
        sbSql.append(" AND to_uid=");
        sbSql.append(toUid);
        sbSql.append(" AND status=0");
        String condition = sbSql.toString();
        List<Map<String, Object>> list = SqlManager.listByCondition("friend_request", condition);
        return !list.isEmpty();
    }

    public Map<String, Object> getRequestById(int requestId) {
        StringBuilder sbSql = new StringBuilder();
        sbSql.append("id=");
        sbSql.append(requestId);
        String condition = sbSql.toString();
        List<Map<String, Object>> list = SqlManager.listByCondition("friend_request", condition);
        return list.isEmpty() ? null : list.get(0);
    }

    public Map<String, Object> getById(int requestId) {
        return getRequestById(requestId);
    }

    public List<Map<String, Object>> listPendingRequests(int toUid) {
        StringBuilder sbSql = new StringBuilder();
        sbSql.append("to_uid=");
        sbSql.append(toUid);
        sbSql.append(" AND status=0 ORDER BY create_time DESC");
        String condition = sbSql.toString();
        return SqlManager.listByCondition("friend_request", condition);
    }

    public List<Map<String,Object>> getUnHandledRequest(int toUid){
        StringBuilder sbSql = new StringBuilder();
        sbSql.append("to_uid=");
        sbSql.append(toUid);
        sbSql.append(" AND status=0 ORDER BY create_time DESC");
        String condition = sbSql.toString();
        return SqlManager.listByCondition("friend_request", condition);
    }

    public static List<FriendRequestPushMsg> queryUnPushRequest(int toUid) {
        StringBuilder sbCond = new StringBuilder();
        sbCond.append("to_uid=");
        sbCond.append(toUid);
        sbCond.append(" AND status=0 AND is_pushed=0");
        String condition = sbCond.toString();
        System.out.println("【调试条件】"+condition);
    
        List<Map<String, Object>> rawList = SqlManager.listByCondition("friend_request", condition);
        System.out.println("【原始查询行数】"+rawList.size());
        List<FriendRequestPushMsg> result = new ArrayList<>();
    
        if (rawList == null || rawList.isEmpty()) {
            return result;
        }
    
        for (Map<String, Object> map : rawList) {
            FriendRequestPushMsg item = new FriendRequestPushMsg();
            item.setRequestId((Integer) map.get("id"));
            item.setFromUid((Integer) map.get("from_uid"));
            item.setFromNickname((String) map.get("from_nickname"));
            item.setReqMsg((String) map.get("req_msg"));
            Object timeObj = map.get("create_time");
            if(timeObj != null){
                item.setCreateTime(timeObj.toString());
            } else {
                item.setCreateTime("");
            }
            result.add(item);
        }
        return result;
    }

    public static int markPushed(int requestId) {
        StringBuilder sbSql = new StringBuilder();
        sbSql.append("UPDATE friend_request SET is_pushed=1 WHERE id=");
        sbSql.append(requestId);
        String sql = sbSql.toString();
        return SqlManager.update(sql);
    }

    public int handleRequest(int requestId, int status) {
        StringBuilder sbSql = new StringBuilder();
        sbSql.append("UPDATE friend_request SET status=");
        sbSql.append(status);
        sbSql.append(" WHERE id=");
        sbSql.append(requestId);
        sbSql.append(" AND status=0");
        String sql = sbSql.toString();
        return SqlManager.update(sql);
    }

    public int updateStatus(int requestId, int status) {
        return handleRequest(requestId, status);
    }
}
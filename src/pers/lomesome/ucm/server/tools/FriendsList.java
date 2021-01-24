package pers.lomesome.ucm.server.tools;

/**
 * 功能：查询用户好友以及添加好友
 */

import pers.lomesome.ucm.common.PeopleInformation;
import pers.lomesome.ucm.server.db.SqlHelper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FriendsList {

    public static List<PeopleInformation> getFriends(String id) throws SQLException {
        List<PeopleInformation> friends = new ArrayList<>();
        String sql = "SELECT friendslist.note,userinformation.*  FROM friendslist  join userinformation ON friendslist.user2 = userinformation.userid WHERE friendslist.user1 = ?";
        String[] paras = { id };
        SqlHelper sh = new SqlHelper();
        ResultSet rs = sh.queryExecute(sql, paras);
        while (rs.next()) {
            PeopleInformation friend = new PeopleInformation(rs.getString("userid"), rs.getString("nickname"), rs.getString("note"), rs.getString("head"), rs.getString("sex"), rs.getString("age"), rs.getString("signature"), rs.getString("phonenumber"), rs.getString("birthday"));
            friends.add(friend);
        }
        sh.close();
        return friends;
    }

    public static void addF_F(String id,String id_){
        SqlHelper sh = new SqlHelper();
        String sql = "insert into friendslist (user1, user2, note, friendgroup) values(?, ?, ?, ?)";
        String[] paras = { id, id_, null, null};
        sh.changeMsg(sql, paras);
        sh.close();
    }

}

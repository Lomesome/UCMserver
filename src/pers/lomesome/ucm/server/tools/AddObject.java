package pers.lomesome.ucm.server.tools;

/**
 * 功能：添加好友界面查询用户信息
 */

import pers.lomesome.ucm.common.PeopleInformation;
import pers.lomesome.ucm.server.db.SqlHelper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AddObject {

    public static List<PeopleInformation> getObjects(String id) throws SQLException {
        List<PeopleInformation> peopleInformations = new ArrayList<>();
        String sql = "SELECT *  FROM userinformation where userid = ?";
        String[] paras = { id };
        SqlHelper sh = new SqlHelper();
        ResultSet rs = sh.queryExecute(sql, paras);
        while (rs.next()) {
            PeopleInformation peopleInformation = new PeopleInformation(rs.getString("userid"), rs.getString("nickname"), null, rs.getString("head"), rs.getString("sex"), rs.getString("age"), rs.getString("signature"), rs.getString("phonenumber"), rs.getString("birthday"));
            peopleInformations.add(peopleInformation);
        }
        sh.close();
        return peopleInformations;
    }
}

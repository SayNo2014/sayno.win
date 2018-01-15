package win.sayno.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OracleJDBC {
	public static void main(String[] args) {

		Connection con;

		PreparedStatement preparedStatement;

		ResultSet resultSet;
		
		try {
//			Class.forName("oracle.jdbc.driver.OracleDriver");
			con = DriverManager.getConnection("jdbc:oracle:thin:@192.168.123.135:1521:orcl", "sayno", "sayno");
			preparedStatement = con.prepareStatement("select t.*, t.rowid from T_RPT_APP t where t.app_id = ?");
			preparedStatement.setString(1, "1");

			resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
				System.out.println("app_id:" + resultSet.getString("app_id") + ";APP_NAME:" + resultSet.getString("APP_NAME"));
			}
			
			resultSet.close();
			preparedStatement.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
		}
	}
}


import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;

public class TailerApp {

    private static class NewLineListener extends TailerListenerAdapter {

        @Override
        public void handle(String line) {

            String sql = "INSERT INTO httpaccesslog( `server`,  `port`,  `remotehost`,  `rfc931`,  `authuser`,  `datetime`, `request`,  `status`,  `bytes` ) values(?,?,?,?,?,?,?,?,?)";

            System.out.println(line);
            String regex = "^([\\S+]+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+-]\\d{4})\\] \"(.+?)\" (\\d{3}) (\\d+) ";

            //String WeblogicLogSample = "10.11.12.13 - - [27/7月/2022:05:55:58 +0800] \"GET /index.jsp HTTP/1.1\" 200 906 ";
            Pattern p = Pattern.compile(regex, Pattern.UNICODE_CHARACTER_CLASS);
            System.out.println("Weblogic log input line: " + line);
            Matcher matcher = p.matcher(line);
            if (matcher.find()) {
                String remotehost = matcher.group(1);
                System.out.println(remotehost);

                String rfc931 = matcher.group(2);
                System.out.println(rfc931);

                String authuser = matcher.group(3);
                System.out.println(authuser);

                Date now = new Date(); // 创建一个Date对象，获取当前时间
                // 指定格式化格式
                SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy:HH:mm:ss Z");
                System.out.println(f.format(now)); // 将当前时间袼式化为指定的格式

                String date = matcher.group(4);
                System.out.println(date);
                date = date.replace("1月", "01");
                date = date.replace("2月", "02");
                date = date.replace("3月", "03");
                date = date.replace("4月", "04");
                date = date.replace("5月", "05");
                date = date.replace("6月", "06");
                date = date.replace("7月", "07");
                date = date.replace("8月", "08");
                date = date.replace("9月", "09");
                date = date.replace("10月", "10");
                date = date.replace("11月", "11");
                date = date.replace("12月", "12");
                date = date.replace("一月", "01");
                date = date.replace("二月", "02");
                date = date.replace("三月", "03");
                date = date.replace("四月", "04");
                date = date.replace("五月", "05");
                date = date.replace("六月", "06");
                date = date.replace("七月", "07");
                date = date.replace("八月", "08");
                date = date.replace("九月", "09");
                date = date.replace("十月", "10");
                date = date.replace("十一月", "11");
                date = date.replace("十二月", "12");
                String strDateFormat = "dd/MM/yyyy:HH:mm:ss Z";
                SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
                Date datetime = null;
                Timestamp timestamp = null;
                try {
                    datetime = sdf.parse(date);
                    System.out.println(datetime);
                    timestamp = new java.sql.Timestamp(datetime.getTime()); //再转换为sql.Date对象
                } catch (ParseException ex) {
                    Logger.getLogger(TailerApp.class.getName()).log(Level.SEVERE, null, ex);
                }

                String request = matcher.group(5);
                System.out.println(request);

                int status = Integer.parseInt(matcher.group(6));
                System.out.println(status);

                int bytes = Integer.parseInt(matcher.group(7));
                System.out.println(bytes);

                Connection con = null;
                PreparedStatement ps = null;
                int result = 0;
                try {

                    // establish the connection
                    con = DriverManager.getConnection("jdbc:mysql://localhost:3306/weblogiclog?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai", "root", "root");

                    // compile SQL query and
                    // store it in PreparedStatemet object
                    if (con != null) {
                        ps = con.prepareStatement(sql);
                    }

                    // read multiple set of inputs from end-user
                    // set input values and execute the query
                    if (ps != null) {

                        // set input values to query parameters
                        ps.setString(1, "10.48.178.192");
                        ps.setString(2, "2018");
                        ps.setString(3, remotehost);
                        ps.setString(4, rfc931);
                        ps.setString(5, authuser);
                        ps.setTimestamp(6, timestamp);
                        ps.setString(7, request);
                        ps.setInt(8, status);
                        ps.setInt(9, bytes);

                        // execute the query
                        result = ps.executeUpdate();

                    }

                    // process the result
                    if (result == 0) {
                        System.out.println("\nRecords not inserted");
                    } else {
                        System.out.println("\nRecords inserted"
                                + " successfully");
                    }

                } catch (SQLException se) {
                    se.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                } // end of try-catch block
                finally {
                    // close JDBC objects
                    try {
                        if (ps != null) {
                            ps.close();
                        }
                    } catch (SQLException se) {
                        se.printStackTrace();
                    }
                    try {
                        if (con != null) {
                            con.close();
                        }
                    } catch (SQLException se) {
                        se.printStackTrace();
                    }

                }
            }
        }
    }
    private final File file;
    private final long delay;
    private final TailerListenerAdapter newLineHandler;

    public TailerApp(File file, long delay, TailerListenerAdapter newLineHandler) {
        this.file = file;
        this.delay = delay;
        this.newLineHandler = newLineHandler;
    }

    public void run() {
        Tailer tailer = new Tailer(file, newLineHandler, delay);
        tailer.run();
    }

    public static void main(String[] args) {
        TailerApp tailer = new TailerApp(new File("C:\\oracle\\Middleware\\user_projects\\domains\\base_domain\\servers\\AdminServer\\logs\\access.log"), 500, new NewLineListener());
        tailer.run();
    }
}

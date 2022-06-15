import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SafeSaving {

    private ArrayList<String> arrowsComb = new ArrayList<>();
    private ArrayList<String> password = new ArrayList<>();
    private boolean hintUsed = false;
    private long start;
    private long stop;

    public void throwArrow(String arrow) {arrowsComb.add(arrow);}
    public void throwDecodingKey(ArrayList<Integer> decodingKey){
        for( int i = 0; i < decodingKey.size(); i++)
            //converting decodingKey into String
            password.add(decodingKey.get(i).toString());
    }
    public void hintWasUsed(){
        hintUsed = true;
    }
    public void setStartTime(long startTime) {start = startTime;}
    public void setStopTime(long stopTime) {stop = stopTime;}
    public void createSave(){
        try {
            //taking current date
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            String todayStr = formatter.format(new Date());
            //taking current time
            Long hourLong = System.currentTimeMillis();
            String hourString = hourLong.toString();
            //create the file with name including information above
            OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(todayStr + " " + hourString + ".txt"));

            //sending to file length of game
            writer.write("Game Time: ");
            Long elapsedTime = (stop - start)/1000;
            String elapsedTimeString = elapsedTime.toString();
            writer.write(elapsedTimeString + " s");

            //sending to file correct combination
            writer.write("\r\n");
            writer.write("Correct Password: ");

            for( int i = 0; i < password.size(); i++){
                writer.write(password.get(i));
            }

            //sending to file information about usage of hint
            writer.write("\r\n");
            writer.write("Hint was used: ");

            String hintUsedString = "False";
            if(hintUsed)
                hintUsedString = "True";

            writer.write(hintUsedString);

            //sending to file combination introduced by player
            writer.write("\r\n");
            writer.write("Introduced combination: ");

            for( int i = 0; i < arrowsComb.size(); i++){
                writer.write("\r\n");
                writer.write(arrowsComb.get(i));
            }

            writer.close();

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}

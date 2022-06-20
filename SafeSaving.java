import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class SafeSaving
{

    private ArrayList<String> arrowsComb = new ArrayList<>();
    private ArrayList<String> password = new ArrayList<>();
    private boolean hintUsed = false;
    private long start;
    private long stop;


    public void throwArrow(String arrow) {
        arrowsComb.add(arrow);
    }

    public void throwDecodingKey(ArrayList<Integer> decodingKey) {
        for( int i = 0; i < decodingKey.size(); i++)
            password.add(decodingKey.get(i).toString());    // converting decodingKey into String
    }

    public void hintWasUsed(){
        hintUsed = true;
    }

    public void setStartTime(long startTime) {
        start = startTime;
    }

    public void setStopTime(long stopTime) {
        stop = stopTime;
    }

    public String getElapsedTime() {
        Long elapsedTime = (stop - start)/1000;
        return elapsedTime.toString();
    }


    public void createSave(){
        try {
            // taking current date and time
            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyy hh-mm-ss");
            String todayStr = formatter.format(new Date());

            // creating the file with name including information above
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream("savings/" + todayStr + ".txt"));

            // sending to file length of game
            writer.write("Game Time: ");
            Long elapsedTime = (stop - start)/1000;
            String elapsedTimeString = elapsedTime.toString();
            writer.write(elapsedTimeString + " s");

            // sending to file correct combination
            writer.write("\r\n");
            writer.write("Correct Password: ");

            for( int i = 0; i < password.size(); i++)
                writer.write(password.get(i));

            // sending to file information about usage of hint
            writer.write("\r\n");
            writer.write("Hint was used: ");

            String hintUsedString = "False";
            if(hintUsed)
                hintUsedString = "True";

            writer.write(hintUsedString);

            // sending to file combination introduced by player
            writer.write("\r\n");
            writer.write("Introduced combination: ");

            writer.write(arrowsComb.get(0));
            for( int i = 1; i < arrowsComb.size(); i++){
                writer.write("-");
                writer.write(arrowsComb.get(i));
            }

            writer.close();
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

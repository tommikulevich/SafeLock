import javax.media.j3d.*;
import javax.swing.*;
import javax.vecmath.*;
import java.awt.*;


public class SafeInteraction
{

    // Game parameters
    public int whatCyl = 0;
    public int stepNum = 0;
    public float angleMain = 0;
    public float angle = 0;
    public int gameEnded = 0;
    public boolean clockwiseDir;
    public boolean nextCyl;

    // Latches after clicking buttons
    public boolean latchLeft = false;
    public boolean latchRight = false;

    // Auxiliary variables for animations
    public float dyWBox = 0;
    public float dzRoof = 0;
    public float dt = 0;

    // Variables and objects forwarded from another class
    public boolean leftButton;
    public boolean rightButton;
    public PointSound tick;
    public PointSound tickNext;
    public SafeCreation sC;
    public SafeSaving sS;


    public void safeGame(boolean lb, boolean rb, PointSound t, PointSound tn, SafeCreation safeCreation, SafeSaving safeSaving)
    // Game realization
    {
        leftButton = lb;
        rightButton = rb;
        tick = t;
        tickNext = tn;
        sC = safeCreation;
        sS = safeSaving;

        // if the game is not over yet
        if(gameEnded == 0)
            if(whatCyl != sC.numOfCyl || !nextCyl)      // in other words: if the user has not reached the situation
                setAngle();                             // when he turns the last cylinder, which is located in the correct position
            else
                gameEnded = 1;

        // if the user has found the right combination, the winning box goes down
        if(gameEnded == 1) {
            Transform3D boxTrans = new Transform3D();
            boxTrans.setTranslation(new Vector3f(0f, (sC.vertPos+sC.cylRad*1.5f+sC.wBoxHeight)- dyWBox, (sC.disBetCyl*(sC.posOfFirstCyl+sC.posOfLastCyl+0.5f)/2) - sC.cylH/2));
            sC.moveBox.setTransform(boxTrans);

            dyWBox += 0.1f;

            if (dyWBox >= sC.cylRad*1.5f) {
                gameEnded = 2;
                sS.setStopTime(System.currentTimeMillis());     // sending to sS current time
            }
        }
    }


    private void setAngle()
    // Initialize the rotation of the cylinders depending on the pressed button
    {
        if(leftButton) {
            if(whatCyl == 0) {                          // if the user started by rotating to the left
                clockwiseDir = false;
                setStepsNum();
                whatCyl++;
            }

            latchLeft = true;
        }
        else if(latchLeft) {
            if(nextCyl) {                               // if the user can rotate the next cylinder ...
                if(!clockwiseDir) {                     // ... and he rotates in the right direction
                    whatCyl++;
                    angle = 0;
                    stepNum = 0;
                }
                else                                    // if he continued to rotate in the same direction as before ...
                    clockwiseDir = !clockwiseDir;       // ... the rotating side changes and he forced to rotate further in that direction

                nextCyl = false;
            }

            if (!clockwiseDir) {                        // if the user rotates to the left (so he can't rotates to the right)
                angleMain += Math.PI/5;
                angle += Math.PI/5;
                rotateCyl();
                checkKey();
                dispActNums();
            }

            sS.throwArrow("L");     // sending to sS information about pushed button

            latchLeft = false;
        }

        if (rightButton) {
            if(whatCyl == 0) {                          // if the user started by rotating to the right
                clockwiseDir = true;
                setStepsNum();
                whatCyl++;
            }

            latchRight = true;
        }
        else if(latchRight) {
            if(nextCyl) {                               // if the user can rotate the next cylinder ...
                if(clockwiseDir) {                      // ... and he rotates in the right direction
                    whatCyl++;
                    angle = 0;
                    stepNum = 0;
                }
                else                                    // if he continued to rotate in the same direction as before ...
                    clockwiseDir = !clockwiseDir;       // ... the rotating side changes and he forced to rotate further in that direction

                nextCyl = false;
            }

            if (clockwiseDir) {                         // if the user rotates to the right (so he can't rotates to the left)
                angleMain -= Math.PI/5;
                angle -= Math.PI/5;
                rotateCyl();
                checkKey();
                dispActNums();
            }

            sS.throwArrow("R");     // sending to sS information about pushed button

            latchRight = false;
        }
    }


    private void setStepsNum()
    // Calculates how many steps the user needs to take in order for the position of this cylinder to be correct
    {
        if(clockwiseDir) {                                  // if the user makes a turn to the right side at the beginning of the game
            for(int i = 0; i < sC.numOfCyl; i++)
                if(i % 2 == 0)
                    sC.stepsKey.add(10-sC.decodingKey.get(i));
                else
                    sC.stepsKey.add(sC.decodingKey.get(i));
        }
        else {                                              // otherwise
            for(int i = 0; i < sC.numOfCyl; i++)
                if(i % 2 == 0)
                    sC.stepsKey.add(sC.decodingKey.get(i));
                else
                    sC.stepsKey.add(10-sC.decodingKey.get(i));
        }
    }


    private void rotateCyl()
    // Rotates the main and secondary cylinders
    {
        if((int)Math.abs(Math.round((angleMain/(Math.PI/5)))) == 10)
            angleMain = 0;

        Transform3D rotMain = new Transform3D();
        rotMain.rotY(angleMain);
        sC.rotCyl.get(0).setTransform(rotMain);

        Transform3D rot = new Transform3D();
        rot.rotY(angle);
        sC.rotCyl.get(whatCyl).setTransform(rot);

        stepNum++;
    }


    private void checkKey()
    // Checks whether the user has taken as many steps as necessary for the cylinder to be in the correct position
    {
        if(whatCyl != 0 && whatCyl <= sC.numOfCyl) {
            if(stepNum == sC.stepsKey.get(whatCyl-1)) {
                nextCyl = true;                         // the user can start rotating the next cylinder
                clockwiseDir = !clockwiseDir;           // changing the rotating side

                tick.setEnable(false);
                tickNext.setEnable(false);
                tickNext.setEnable(true);               // turning on the sound of the correct cylinder position
            }
            else {
                nextCyl = false;

                if(stepNum == 10) {                     // if the user has completed a full turn
                    stepNum = 0;
                    angle = 0;
                }

                tickNext.setEnable(false);
                tick.setEnable(false);
                tick.setEnable(true);
            }
        }
        else
            tick.setEnable(true);
    }


    private void dispActNums()
    // Setting up and displaying the actual digit entered by the user
    {
        int number = (int)Math.round(angleMain/(Math.PI/5) % 10);

        if(number >= 0)
            sC.dispNums.get(whatCyl-1).setString(Integer.toString(number));
        else
            sC.dispNums.get(whatCyl-1).setString(Integer.toString(10-Math.abs(number)));
    }


    public boolean lookInside()
    // Animation of removing the top cover of the box - easy level hint
    {
        if (dzRoof >= sC.disBetCyl*(sC.posOfFirstCyl-sC.posOfLastCyl)+sC.preDefDim)
            return false;

        Transform3D boxTrans = new Transform3D();
        boxTrans.setTranslation(new Vector3f(0f, sC.vertPos+sC.cylRad*1.5f+(sC.wBoxHeight), (sC.disBetCyl*(sC.posOfFirstCyl+sC.posOfLastCyl+0.5f)/2) - sC.cylH/2 - dzRoof));
        sC.setBoxPos.get(3).setTransform(boxTrans);

        dzRoof += 0.1f;

        return true;
    }


    public boolean giveDigit()
    // Showing the correct combination number within a few seconds - medium level hint
    {
        if(gameEnded == 0 && whatCyl != 0) {
            if (dt >= 10.0f) {
                dispActNums();

                return false;
            }

            sC.dispNums.get(whatCyl-1).setString(Integer.toString(sC.password.get(whatCyl-1)));
            dt += 0.1f;

            return true;
        }

        return false;
    }


    public boolean stepBack()
    // Going back one digit (step back) - hard level hint
    {
        if(gameEnded == 0 && whatCyl != 0) {
            if (!clockwiseDir) {
                angleMain -= Math.PI/5;
                angle -= Math.PI/5;
                rotateCyl();
                stepNum -= 2;
                checkKey();
                dispActNums();
            }
            else {
                angleMain += Math.PI/5;
                angle += Math.PI/5;
                rotateCyl();
                stepNum -= 2;
                checkKey();
                dispActNums();
            }

            return false;
        }

        return true;
    }


    public void info(Component window)
    // Showing information about the game
    {
        String header = "Safe Lock. About this game";

        String info = "<html> &#10003; You need to find the right combination of the safe with the help of sounds. <br>" +
                             "&#10003; You can use one hint, which depends on the difficulty level of the game. <br><br>" +
                             "<u>Common for all modes</u>: <br>" +
                             "<ul><li><b>Start New Game</b> &#8594; Choose level of difficulty from the list and approve it.<br>" +
                             "<li><b>Pause/Continue</b>     &#8594; Stopping/Rerunning the game at any time.<br>" +
                             "<li><b>Default View</b>       &#8594; Returning to start view after change of camera position.<br>" +
                             "<li><b>Hint</b>               &#8594; Supporting information that can be used once per game, more details below.<br>" +
                             "<li><b>Save</b>               &#8594; This button appears after end of the game, you will be able to save most of <br>" +
                             "interesting information about your game in savings folder." +
                             "<li><b>Left/Right arrow</b>   &#8594; Controlling rotation of the mechanism.<br></ul>" +
                             "<u>Specification of hints for game modes</u>: <br>" +
                             "<ul><li><i>Easy</i> &#8594; The lid of the box opens to show mechanism inside the box.<br>" +
                             "<li><i>Medium</i>   &#8594; On the right side of the box the information about current number<br>" +
                             "changes to correct number for a few seconds.<br>" +
                             "<li><i>Hard</i>     &#8594; You will be able to return to the previous position of the mechanism.</ul>" +
                             "Good luck! &#128151; </html>";

        JLabel label = new JLabel(info);
        label.setFont(new Font("sans-serif", Font.PLAIN, 14));

        JOptionPane.showMessageDialog(window, label, header, JOptionPane.INFORMATION_MESSAGE);
    }


    public void winInfo(Component window)
    // Showing information about winning the game
    {
        String info = "Your time: " + sS.getElapsedTime() + " s";
        String header = "WIN! Congratulations!";

        JOptionPane.showMessageDialog(window, info, header, JOptionPane.INFORMATION_MESSAGE);
    }

}
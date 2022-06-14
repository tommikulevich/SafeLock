import javax.media.j3d.*;
import javax.swing.*;
import javax.vecmath.*;
import java.awt.*;


public class SafeInteraction
//
{

    public boolean latchLeft = false;
    public boolean latchRight = false;

    // game parameters
    public float angle = 0;
    public float angleMain = 0;
    public int whatCyl = 0;
    public int stepNum = 0;
    public int gameEnded = 0;
    public boolean clockwiseDir;
    public boolean nextCyl;
    public float dyWBox = 0;
    public float dxRoof = 0f;

    public boolean isStepBack = false;

    public boolean leftButton;
    public boolean rightButton;
    public PointSound tick;
    public PointSound tickNext;
    public SafeCreation sC;


    public void safeGame(boolean lb, boolean rb, PointSound t, PointSound tn, SafeCreation safeCreation)
    //
    {
        leftButton = lb;
        rightButton = rb;
        tick = t;
        tickNext = tn;
        sC = safeCreation;

        if(gameEnded == 0)
            if(whatCyl != sC.numOfCyl || !nextCyl)     // in other words: if the user has not reached the situation
                setAngle();                         // when he turns the last cylinder, which is located in the correct position
            else
                gameEnded = 1;

        if(gameEnded == 1) {
            Transform3D boxTrans = new Transform3D();
            boxTrans.setTranslation(new Vector3f(0f, (sC.vertPos+sC.cylRad*1.5f+sC.wBoxHeight)- dyWBox, (sC.disBetCyl*(sC.posOfFirstCyl+sC.posOfLastCyl+0.5f)/2) - sC.cylH/2));
            sC.moveBox.setTransform(boxTrans);

            dyWBox += 0.1f;

            if (dyWBox >= sC.cylRad*1.5f)
                gameEnded = 2;
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
            }

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
            }

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


    public boolean hintFloor()
    //
    {
        if (dxRoof >= sC.cylRad*4f)
            return false;

        Transform3D boxTrans = new Transform3D();
        boxTrans.setTranslation(new Vector3f(0f + dxRoof, sC.vertPos+sC.cylRad*1.5f+(sC.wBoxHeight), (sC.disBetCyl*(sC.posOfFirstCyl+sC.posOfLastCyl+0.5f)/2) - sC.cylH/2));
        sC.setBoxPos.get(3).setTransform(boxTrans);

        dxRoof += 0.1f;

        return true;
    }


    public void stepBack()
    //
    {
        if(gameEnded == 0 && isStepBack == false) {
            if (!clockwiseDir) {
                angleMain -= Math.PI/5;
                angle -= Math.PI/5;
                rotateCyl();
                stepNum -= 2;
                checkKey();
            }
            else {
                angleMain += Math.PI/5;
                angle += Math.PI/5;
                rotateCyl();
                stepNum -= 2;
                checkKey();
            }

            isStepBack = true;
        }
    }


    public void info(Component window)
    //
    {
        String info = "Tutaj znajdzie się info o: \n - zasadach \n - autorach";
        String header = "About this project";

        JOptionPane.showMessageDialog(window, info, header, JOptionPane.INFORMATION_MESSAGE);
    }
}
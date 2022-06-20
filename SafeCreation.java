import java.awt.*;
import java.util.ArrayList;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.geometry.*;
import javax.media.j3d.*;
import javax.vecmath.*;

import static javax.media.j3d.TransformGroup.ALLOW_TRANSFORM_WRITE;


public class SafeCreation
{

    // Parameters of cylinders
    public ArrayList<Cylinder>cylinders = new ArrayList<>();
    public int numOfCyl;
    public float disBetCyl = 1.0f;
    public float vertPos = 3.0f;
    public float cylRad = 1.5f;
    public float cylH = 0.5f;
    public float axRad = 0.1f;
    public float posOfFirstCyl;
    public float posOfLastCyl = 0;

    // Parameters of winning box
    public float wBoxHeight = 0.75f;

    // Parameters of case
    public ArrayList<Box> caseBoxes = new ArrayList<>();
    public ArrayList<TransformGroup> setBoxPos = new ArrayList<>();
    public Float[][] caseDim = new Float[6][3];
    public Float[][] caseWallsPos = new Float[6][3];
    public float preDefDim = 0.1f;

    // Texture parameters
    public Appearance lightMetal = new Appearance();
    public Appearance darkMetal = new Appearance();
    public Appearance veryDarkMetal = new Appearance();
    public TextureLoader loader;
    public Texture2D texture;
    public ImageComponent2D image;

    // Transform groups and branches
    public ArrayList<TransformGroup>rotCyl = new ArrayList<>();
    public TransformGroup tgRot;
    public TransformGroup moveBox;
    public BranchGroup tgRotBG = new BranchGroup();
    public BranchGroup moveBoxBG = new BranchGroup();
    public BranchGroup caseBoxBG = new BranchGroup();

    // Game parameters
    public ArrayList<Integer> decodingKey = new ArrayList<>();
    public ArrayList<Integer> stepsKey = new ArrayList<>();
    public ArrayList<Integer> password = new ArrayList<>();
    public ArrayList<Text2D>  dispNums = new ArrayList<>();

    // Object of saving
    public SafeSaving sS;


    public SafeCreation(int numOfCylinders, SafeSaving safeSaving)
    // Safe lock creation initialization
    {
        numOfCyl = numOfCylinders;
        posOfFirstCyl = numOfCyl/2.0f;

        sS = safeSaving;

        loadTextures();         // loading textures
        createCylinders();      // creating all cylinders
        createWinningBox();     // creating winning box
        createCase();           // creating safe case
    }


    public void loadTextures()
    // Loading textures
    {
        // light metal texture
        loader = new TextureLoader("img/lightMetal.jpg", null);
        image = loader.getImage();

        texture = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA, image.getWidth(), image.getHeight());
        texture.setImage(0, image);
        texture.setBoundaryModeS(Texture.WRAP);
        texture.setBoundaryModeT(Texture.WRAP);

        lightMetal.setTexture(texture);

        // dark metal texture
        loader = new TextureLoader("img/darkMetal.jpg", null);
        image = loader.getImage();

        texture = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA, image.getWidth(), image.getHeight());
        texture.setImage(0, image);
        texture.setBoundaryModeS(Texture.WRAP);
        texture.setBoundaryModeT(Texture.WRAP);

        darkMetal.setTexture(texture);

        // very dark metal texture
        loader = new TextureLoader("img/veryDarkMetal.jpg", null);
        image = loader.getImage();

        texture = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA, image.getWidth(), image.getHeight());
        texture.setImage(0, image);
        texture.setBoundaryModeS(Texture.WRAP);
        texture.setBoundaryModeT(Texture.WRAP);

        veryDarkMetal.setTexture(texture);

    }

    public void createCylinders()
    // Creating main cylinder and mechanism cylinders
    {
        // ----- MAIN CYLINDER ------

        // setting params for main cylinder
        cylinders.add(new Cylinder(cylRad*1.5f, cylH, Cylinder.GENERATE_NORMALS| Cylinder.GENERATE_TEXTURE_COORDS, 80, 80, veryDarkMetal));

        // creating lines on main cylinder
        for (int i = 0; i < 5; i++) {
            Appearance app = new Appearance();
            app.setColoringAttributes(new ColoringAttributes(new Color3f(Color.LIGHT_GRAY), ColoringAttributes.NICEST));

            Box line = new Box(axRad/1.5f, cylH/1.9f, cylRad*1.2f, app);

            Transform3D lineRot = new Transform3D();
            lineRot.rotY(i*Math.PI/5);

            TransformGroup lineTransform = new TransformGroup(lineRot);
            lineTransform.addChild(line);

            cylinders.get(0).addChild(lineTransform);
        }

        // creating numbers on main cylinder
        for (int i = 0; i < 10; i++){
            Text2D num = new Text2D(Integer.toString(i), new Color3f(Color.WHITE), "SansSerif", 100, Font.BOLD);

            Transform3D numPos = new Transform3D();
            numPos.set(new Vector3f(-0.1f, cylRad*1.2f, cylH/1.9f));

            Transform3D numRot1 = new Transform3D();
            numRot1.rotX(-Math.PI/2);
            numRot1.mul(numPos);

            Transform3D numRot2 = new Transform3D();
            numRot2.rotY(-i*Math.PI/5);

            TransformGroup numTransform1 = new TransformGroup(numRot1);
            numTransform1.addChild(num);

            TransformGroup numTransform2 = new TransformGroup(numRot2);
            numTransform2.addChild(numTransform1);

            cylinders.get(0).addChild(numTransform2);
        }

        // adding cosmetic cylinder
        cylinders.get(0).addChild(new Cylinder(cylRad, cylH+0.5f, Cylinder.GENERATE_NORMALS| Cylinder.GENERATE_TEXTURE_COORDS, 80, 80, veryDarkMetal));


        // ----- KEYS ------

        // filling list of decoding key for cylinders
        for(int i = 0; i < numOfCyl; i++)
            decodingKey.add(randomInt());

        // filling list of password
        int temp = decodingKey.get(0);
        password.add(temp);
        for(int i = 1; i < numOfCyl; i++) {
            temp = (temp + decodingKey.get(i)) % 10;
            password.add(temp);
        }

        //sending to sS generated combination
        sS.throwDecodingKey(password);


        // ----- MECHANISM CYLINDERS ------

        // setting params of mechanism cylinders
        for(int i = 1; i <= numOfCyl; i++)
            cylinders.add(new Cylinder(cylRad, cylH, Cylinder.GENERATE_NORMALS| Cylinder.GENERATE_TEXTURE_COORDS, 80, 80, lightMetal));

        // creating lines on mechanism cylinders
        for (int i = 1; i <= numOfCyl; i++){
            Appearance app = new Appearance();
            app.setColoringAttributes(new ColoringAttributes(new Color3f(Color.BLACK), ColoringAttributes.NICEST));

            Box line = new Box(axRad, cylH/1.9f, cylRad/2.0f, app);

            Transform3D linePos = new Transform3D();
            linePos.set(new Vector3f(0.0f, 0.0f, -cylRad/2));

            TransformGroup lineTransform = new TransformGroup(linePos);
            lineTransform.addChild(line);

            cylinders.get(i).addChild(lineTransform);
        }


        // ----- 3D TRANSFORMATIONS FOR CYLINDERS ------

        // creating transformations of self rotation for cylinders
        ArrayList<Transform3D>selfRotCyl = new ArrayList<>();
        // filling first of theirs self rotation by empty transform
        selfRotCyl.add(new Transform3D());

        // initializing random rotation for cylinders
        for (int i = 0; i < numOfCyl; i++){
            Transform3D tmp_rot = new Transform3D();
            tmp_rot.rotY(-decodingKey.get(i)*Math.PI/5);
            selfRotCyl.add(tmp_rot);
        }

        // filling last of theirs self rotation by empty transform
        selfRotCyl.add(new Transform3D());

        // creating transformations of position for cylinders
        ArrayList<Transform3D>posCyl = new ArrayList<>();

        //setting this transformation by position algorithm
        posOfLastCyl = posOfFirstCyl;
        for (int i = 0; i <= numOfCyl; i++, posOfLastCyl--){
            Transform3D k = new Transform3D();
            k.set(new Vector3f(0, disBetCyl*posOfLastCyl, -vertPos));
            posCyl.add(k);
        }


        // ----- AXIS ------

        // setting params for axis by axis algorithm
        cylinders.add(new Cylinder(axRad, disBetCyl*(posOfFirstCyl-posOfLastCyl-1), Cylinder.GENERATE_NORMALS| Cylinder.GENERATE_TEXTURE_COORDS, 80, 80, lightMetal));

        // creating and setting position transformation for axis
        Transform3D p = new Transform3D();
        p.set(new Vector3f(0, disBetCyl*(posOfFirstCyl+posOfLastCyl+1)/2,-vertPos));
        posCyl.add(p);


        // ----- GROUP TRANSFORMATIONS FOR CYLINDERS AND AXIS ------

        // creating transformation groups and matching with transformation
        ArrayList<TransformGroup>tg = new ArrayList<>();
        for (int i = 0; i <= numOfCyl+1; i++){
            TransformGroup k = new TransformGroup(posCyl.get(i));
            tg.add(k);

            TransformGroup n = new TransformGroup();
            rotCyl.add(n);
        }

        // matching transformation groups of self rotation
        ArrayList<TransformGroup>tgSelfRot = new ArrayList<>();
        for (int i = 0; i <= numOfCyl+1; i++){
            TransformGroup k = new TransformGroup(selfRotCyl.get(i));
            tgSelfRot.add(k);
        }

        // rotation transformation
        Transform3D tmp_rot = new Transform3D();
        tmp_rot.rotX(Math.PI/2);
        tgRot = new TransformGroup(tmp_rot);

        // matching transformation groups with rotation
        for (int i = 0; i <= numOfCyl+1; i++){
            rotCyl.get(i).setCapability(ALLOW_TRANSFORM_WRITE);
            tgSelfRot.get(i).addChild(cylinders.get(i));
            rotCyl.get(i).addChild(tgSelfRot.get(i));
            tg.get(i).addChild(rotCyl.get(i));
            tgRot.addChild(tg.get(i));
        }

        tgRotBG.addChild(tgRot);
    }


    public int randomInt()
    // Generator of numbers from 1 to 9
    {
        int i = (int)(Math.random()*10);
        i = (i == 0) ? randomInt() : i;

        return (i);
    }


    public void createWinningBox()
    // Creating a winning box
    {
        Box winningBox = new Box(axRad, wBoxHeight, disBetCyl*(posOfFirstCyl-posOfLastCyl)/2 - preDefDim/2, Box.GENERATE_NORMALS| Box.GENERATE_TEXTURE_COORDS, darkMetal);

        Transform3D initPos = new Transform3D();
        initPos.set(new Vector3f(0f, vertPos+cylRad*1.5f+(wBoxHeight), disBetCyl*(posOfFirstCyl+posOfLastCyl+0.5f)/2 - cylH/2));

        moveBox = new TransformGroup(initPos);
        moveBox.setCapability(ALLOW_TRANSFORM_WRITE);
        moveBox.addChild(winningBox);

        moveBoxBG.addChild(moveBox);
    }


    public void createCase()
    // Creating a case to hold a mechanism
    {
        float xDim = cylRad*2.0f;
        float yDim = 3f;
        float zDim = disBetCyl*(posOfFirstCyl-posOfLastCyl)/2;

        for(int i = 0; i < 6; i++) {
            // adding dimensions for floor/roof
            caseDim[i][0]   = xDim;         // x
            caseDim[i][1]   = preDefDim;    // y
            caseDim[i][2]   = zDim;         // z

            // adding dimensions for left/right wall
            caseDim[++i][0] = preDefDim;    // x
            caseDim[i][1]   = yDim;         // y
            caseDim[i][2]   = zDim;         // z

            // adding dimensions for front/back wall
            caseDim[++i][0] = xDim;         // x
            caseDim[i][1]   = yDim;         // y
            caseDim[i][2]   = preDefDim;    // z
        }

        float standardZPos = (disBetCyl*(posOfFirstCyl+posOfLastCyl+0.5f)/2) - cylH/2;
        float floorVsRoofPos = 0f;
        float leftVsRightWallPos = -cylRad*2.0f;
        float frontVsBackWallPos = disBetCyl*posOfFirstCyl;

        for(int i = 0; i < 6; i++) {
            // adding position for floor/roof
            caseWallsPos[i][0]   = 0f;                   // x
            caseWallsPos[i][1]   = floorVsRoofPos+0.11f; // y
            caseWallsPos[i][2]   = standardZPos;         // z

            // adding position for left/right wall
            caseWallsPos[++i][0] = leftVsRightWallPos;   // x
            caseWallsPos[i][1]   = vertPos+0.11f;        // y
            caseWallsPos[i][2]   = standardZPos;         // z

            // adding position for front/back wall
            caseWallsPos[++i][0] = 0f;                   // x
            caseWallsPos[i][1]   = vertPos+0.11f;        // y
            caseWallsPos[i][2]   = frontVsBackWallPos;   // z

            floorVsRoofPos = vertPos+cylRad*1.5f+(wBoxHeight);
            leftVsRightWallPos = -leftVsRightWallPos;
            frontVsBackWallPos = disBetCyl*posOfLastCyl;
        }

        // adding walls in order: floor, roof, left, right, front, back
        for(int i = 0; i < 6; i++) {
            caseBoxes.add(new Box(caseDim[i][0], caseDim[i][1] , caseDim[i][2], Box.GENERATE_NORMALS| Box.GENERATE_TEXTURE_COORDS, lightMetal));

            Transform3D initPos = new Transform3D();
            initPos.set(new Vector3f(caseWallsPos[i][0], caseWallsPos[i][1], caseWallsPos[i][2]));

            setBoxPos.add(new TransformGroup(initPos));
            setBoxPos.get(i).setCapability(ALLOW_TRANSFORM_WRITE);
            setBoxPos.get(i).addChild(caseBoxes.get(i));

            caseBoxBG.addChild(setBoxPos.get(i));
        }

        // creating line on floor
        Appearance app = new Appearance();
        app.setColoringAttributes(new ColoringAttributes(new Color3f(Color.BLACK), ColoringAttributes.NICEST));

        Box line = new Box(axRad, preDefDim+0.01f, disBetCyl*(posOfFirstCyl-posOfLastCyl)/2 - 0.01f, app);
        caseBoxes.get(3).addChild(line);

        // creating field for password
        Box fieldBox = new Box(preDefDim+0.01f, 0.7f, zDim+0.01f, Box.GENERATE_NORMALS| Box.GENERATE_TEXTURE_COORDS, veryDarkMetal);

        Transform3D fieldBoxRot = new Transform3D();
        fieldBoxRot.rotY(Math.PI);

        TransformGroup fieldTransform = new TransformGroup(fieldBoxRot);
        fieldTransform.addChild(fieldBox);

        caseBoxes.get(4).addChild(fieldTransform);

        // creating numbers on this field
        float temp = -zDim;
        for(int i = 0; i < numOfCyl; i++) {
            Text2D num = new Text2D("X", new Color3f(Color.LIGHT_GRAY), "SansSerif", 250, Font.BOLD);
            dispNums.add(num);

            Transform3D numPos = new Transform3D();
            numPos.set(new Vector3f(temp+2.0f*i*zDim/numOfCyl+0.3f, -0.5f, preDefDim+0.02f));

            Transform3D numRot = new Transform3D();
            numRot.rotY(-Math.PI/2);
            numRot.mul(numPos);

            TransformGroup numTransform = new TransformGroup(numRot);
            numTransform.addChild(dispNums.get(i));

            fieldBox.addChild(numTransform);
        }
    }


    public void addElements(BranchGroup sceneBG)
    // Adding all elements to our scene - cylinders, winning box and case
    {
        sceneBG.addChild(tgRotBG);
        sceneBG.addChild(moveBoxBG);
        sceneBG.addChild(caseBoxBG);
    }

}
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import com.sun.j3d.audioengines.javasound.JavaSoundMixer;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.behaviors.vp.*;
import javax.media.j3d.*;
import javax.vecmath.*;


public class SafeUniverse extends JPanel implements ActionListener, KeyListener
// Holding the 3D canvas where the loaded elements are displayed
{

    private static final int PWIDTH = 512;      // size of panel
    private static final int PHEIGHT = 512;
    private static final int BOUNDSIZE = 100;   // larger than the world

    private static final Point3d USERPOS = new Point3d(0,5,20); // initial user position

    private final SimpleUniverse su;
    private BranchGroup sceneBG;
    private BoundingSphere bounds;
    private final ViewingPlatform vp;
    private final TransformGroup steerTG;

    private String fileTick = "tick.wav";
    private String fileTickNext = "tickNext.wav";
    private PointSound tick = new PointSound();
    private PointSound tickNext = new PointSound();

    private final Timer clock1;
    private boolean leftButton = false, rightButton = false;
    private final Button startButton = new Button("Run");
    private final Button stopButton = new Button("Stop");
    private final Button setDefaultViewButton = new Button("Default View");

    private final Button hintButton = new Button("Hint");
    private boolean latchLeft = false;
    private boolean latchRight = false;

    private final ArrayList<TransformGroup>rotCyl = new ArrayList<>();
    private TransformGroup moveBox;

    // game parameters
    private ArrayList<Integer> decodingKey = new ArrayList<>();
    private ArrayList<Integer> stepsKey = new ArrayList<>();
    private ArrayList<Integer> password = new ArrayList<>();
    private int whatCyl = 0;
    private int stepNum = 0;
    private int gameEnded = 0;
    private boolean clockwiseDir;
    private boolean nextCyl;

    // parameters of cylinders
    private int numOfCyl = 5;
    private float disBetCyl = 1.0f;
    private float vertPos = 3.0f;
    private float cylRad = 1.5f;
    private float cylH = 0.5f;
    private float axRad = 0.1f;
    private int posOfFirstCyl = 2;
    private int posOfLastCyl = 0;
    private float angle = 0;
    private float angleMain = 0;

    // parameters of winning box
    private float wBoxHeight = 0.75f;
    private float dyWBox = 0;

    // parameters of case
    private ArrayList<Box> caseBoxes = new ArrayList<>();
    private ArrayList<TransformGroup> setBoxPos = new ArrayList<>();
    private Float[][] caseDimensions = new Float[6][3];
    private Float[][] caseWallsPositions = new Float[6][3];
    private float preDefDimension = 0.1f;
    private float dxRoof = 0f;



    public SafeUniverse()
    // A panel holding a 3D canvas
    {
        setLayout(new BorderLayout());
        setOpaque(false);
        setPreferredSize(new Dimension(PWIDTH, PHEIGHT));

        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        Canvas3D canvas3D = new Canvas3D(config);
        add("Center", canvas3D);
        canvas3D.addKeyListener(this);

        JPanel panel = new JPanel();
        panel.add(startButton);
        panel.add(stopButton);
        panel.add(setDefaultViewButton);
        panel.add(hintButton);

        add(""+ "North",panel);
        startButton.addActionListener(this);
        startButton.addKeyListener(this);
        stopButton.addActionListener(this);
        stopButton.addKeyListener(this);
        setDefaultViewButton.addKeyListener(this);
        setDefaultViewButton.addActionListener(this);
        hintButton.addKeyListener(this);
        hintButton.addActionListener(this);

        clock1 = new Timer(10, this);

        canvas3D.setFocusable(true);     // giving focus to the canvas
        canvas3D.requestFocus();
        su = new SimpleUniverse(canvas3D);

        vp = su.getViewingPlatform();
        steerTG = vp.getViewPlatformTransform();
        createSceneGraph();
        initUserPosition();        // setting user's viewpoint
        orbitControls(canvas3D);   // controlling the movement of the viewpoint

        // create a sounds mixer to use our sounds with and initialise it
        JavaSoundMixer myMixer = new JavaSoundMixer(su.getViewer().getPhysicalEnvironment());
        myMixer.initialize();

        su.addBranchGraph(sceneBG);
    }


    private void createSceneGraph()
    // Initializing the scene
    {
        sceneBG = new BranchGroup();
        bounds = new BoundingSphere(new Point3d(0,0,0), BOUNDSIZE);

        lightScene();                                   // adding the lights
        addBackground();                                // adding the sky
        sceneBG.addChild(new SafePlatform().getBG());   // adding the floor
        addSounds(tick, fileTick);                      // adding the sounds
        addSounds(tickNext, fileTickNext);

        floatingCylinders();    // adding floating cylinders
        createWinningBox();     // it is required to add winningBox after floatingCylinders
        createMCase();          // it is required to add Case after floatingCylinders

        sceneBG.compile();      // fixing the scene
    }


    private void lightScene()
    // Adding one ambient light and 2 directional lights
    {
        Color3f white = new Color3f(1.0f, 1.0f, 1.0f);

        // setting up the ambient light
        AmbientLight ambientLightNode = new AmbientLight(white);
        ambientLightNode.setInfluencingBounds(bounds);
        sceneBG.addChild(ambientLightNode);

        // setting up the directional lights
        Vector3f light1Direction  = new Vector3f(-1.0f, -1.0f, -1.0f);  // left, down, backwards
        Vector3f light2Direction  = new Vector3f(1.0f, -1.0f, 1.0f);    // right, down, forwards

        DirectionalLight light1 = new DirectionalLight(white, light1Direction);
        light1.setInfluencingBounds(bounds);
        sceneBG.addChild(light1);

        DirectionalLight light2 = new DirectionalLight(white, light2Direction);
        light2.setInfluencingBounds(bounds);
        sceneBG.addChild(light2);
    }


    private void addBackground()
    // Adding a blue sky
    {
        Background back = new Background();
        back.setApplicationBounds(bounds);
        back.setColor(0.17f, 0.65f, 0.92f);    // sky colour

        sceneBG.addChild(back);
    }


    private void addSounds(PointSound sound, String filename)
    // Adding sounds
    {
        // create the media container to load the sound
        MediaContainer soundContainer = new MediaContainer("file:audio/" + filename);
        Vector3f objPosition = new Vector3f();

        // use the loaded data in the sound
        sound.setSoundData(soundContainer);
        sound.setInitialGain(1.0f);
        sound.setPosition(new Point3f(objPosition));

        // allow use to switch the sound on and off
        sound.setCapability(PointSound.ALLOW_ENABLE_READ);
        sound.setCapability(PointSound.ALLOW_ENABLE_WRITE);
        sound.setSchedulingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 1000.0));

        // set it off to start with
        sound.setEnable(false);

        // use the edge value to set to extent of the sound
        Point2f[] attenuation = { new Point2f(0.0f, 1.0f), new Point2f(1000.0f, 0.1f) };
        sound.setDistanceGain(attenuation);

        sceneBG.addChild(sound);
    }


    private void orbitControls(Canvas3D c)
    // OrbitBehaviour allows the user to rotate around the scene, and to zoom in and out
    {
        OrbitBehavior orbit = new OrbitBehavior(c, OrbitBehavior.REVERSE_ALL);
        orbit.setSchedulingBounds(bounds);

        ViewingPlatform vp = su.getViewingPlatform();
        vp.setViewPlatformBehavior(orbit);
    }


    private void initUserPosition()
    // Setting the user's initial viewpoint using lookAt()
    {
        Transform3D t3d = new Transform3D();
        steerTG.getTransform(t3d);

        // args are: viewer pos, where looking, up direction
        t3d.lookAt(USERPOS, new Point3d(0,2,5), new Vector3d(0,4,0));
        t3d.invert();

        steerTG.setTransform(t3d);
    }


    public void floatingCylinders()
    // Creating floating cylinders
    {
        // loading textures
        Appearance woodStyle = new Appearance();
        TextureLoader loader = new TextureLoader("img/wood.png", null);
        ImageComponent2D image = loader.getImage();

        Texture2D wood = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA, image.getWidth(), image.getHeight());
        wood.setImage(0, image);
        wood.setBoundaryModeS(Texture.WRAP);
        wood.setBoundaryModeT(Texture.WRAP);

        woodStyle.setTexture(wood);

        // creating cylinders
        ArrayList<Cylinder>cylinders = new ArrayList<>();

        // setting params for main cylinder
        cylinders.add(new Cylinder(cylRad*1.5f, cylH, Cylinder.GENERATE_NORMALS| Cylinder.GENERATE_TEXTURE_COORDS, 80, 80, woodStyle));

        // creating lines on main cylinder
        for (int i = 0; i < 5; i++) {
            Appearance app = new Appearance();
            app.setColoringAttributes(new ColoringAttributes(new Color3f(Color.BLACK), ColoringAttributes.NICEST));

            Box line = new Box(axRad, cylH/1.9f, cylRad*1.2f, app);

            Transform3D lineRot = new Transform3D();
            lineRot.rotY(i*Math.PI/5);

            TransformGroup lineTransform = new TransformGroup(lineRot);
            lineTransform.addChild(line);

            cylinders.get(0).addChild(lineTransform);
        }

        // creating numbers on main cylinder
        for (int i = 0; i < 10; i++){
            Text2D num = new Text2D(Integer.toString(i), new Color3f(Color.BLACK), "SansSerif", 100, Font.BOLD );

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

        // setting params of other cylinders
        for(int i = 1; i <= numOfCyl; i++)
            cylinders.add(new Cylinder(cylRad, cylH, Cylinder.GENERATE_NORMALS| Cylinder.GENERATE_TEXTURE_COORDS, 80, 80, woodStyle));

        // creating lines on other cylinders
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

        for(int i = 0; i < numOfCyl; i++)
            System.out.println(password.get(i));

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

        // setting params for axis by axis algorithm
        cylinders.add(new Cylinder(axRad, disBetCyl*(posOfFirstCyl-posOfLastCyl-1), Cylinder.GENERATE_NORMALS| Cylinder.GENERATE_TEXTURE_COORDS, 80, 80, woodStyle));

        // creating and setting position transformation for axis
        Transform3D p = new Transform3D();
        p.set(new Vector3f(0, disBetCyl*(posOfFirstCyl+posOfLastCyl+1)/2,-vertPos));
        posCyl.add(p);

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
        TransformGroup tg_rot = new TransformGroup(tmp_rot);

        // matching transformation groups with rotation
        for (int i = 0; i <= numOfCyl+1; i++){
            rotCyl.get(i).setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            tgSelfRot.get(i).addChild(cylinders.get(i));
            rotCyl.get(i).addChild(tgSelfRot.get(i));
            tg.get(i).addChild(rotCyl.get(i));
            tg_rot.addChild(tg.get(i));
        }

        // adding rotation to scene
        sceneBG.addChild(tg_rot);
    }


    public void createWinningBox()
    // Creating a winning box
    {
        Appearance woodStyle = new Appearance();
        TextureLoader loader = new TextureLoader("img/wood.png", null);
        ImageComponent2D image = loader.getImage();

        Texture2D wood = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA, image.getWidth(), image.getHeight());
        wood.setImage(0, image);
        wood.setBoundaryModeS(Texture.WRAP);
        wood.setBoundaryModeT(Texture.WRAP);

        woodStyle.setTexture(wood);

        Box winningBox = new Box(axRad, wBoxHeight, disBetCyl*(posOfFirstCyl-posOfLastCyl-1)/2, woodStyle);

        Transform3D initPos = new Transform3D();
        initPos.set(new Vector3f(0f, vertPos+cylRad*1.5f+(wBoxHeight), (disBetCyl*(posOfFirstCyl+posOfLastCyl+0.5f)/2) - cylH/2));

        moveBox = new TransformGroup(initPos);
        moveBox.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        moveBox.addChild(winningBox);

        sceneBG.addChild(moveBox);
    }

    public void createMCase()
    // Creating a case to hold a mechanism
    {
        float xDimension = cylRad*1.5f;
        float yDimension = 3f;
        float zDimension = disBetCyl*(posOfFirstCyl-posOfLastCyl)/2;

        int i = 0;
        while (i < 6) {
            //adding dimensions for floor/roof
            caseDimensions[i][0]   = xDimension;        //x
            caseDimensions[i][1]   = preDefDimension;   //y
            caseDimensions[i][2]   = zDimension;        //z
            //adding dimensions for lef/right wall
            caseDimensions[++i][0] = preDefDimension;   //x
            caseDimensions[i][1]   = yDimension;        //y
            caseDimensions[i][2]   = zDimension;        //z
            //adding dimensions for front/back wall
            caseDimensions[++i][0] = xDimension;        //x
            caseDimensions[i][1]   = yDimension;        //y
            caseDimensions[i][2]   = preDefDimension;   //z
            i++;
        }

        float standardZPos = (disBetCyl*(posOfFirstCyl+posOfLastCyl+0.5f)/2) - cylH/2;
        float floorVsRoofPos = 0f;
        float leftVsRightWallPos = -cylRad*1.5f;
        float frontVsBackWallPos = disBetCyl*posOfFirstCyl;

        i = 0;
        while (i < 6) {
            //adding position for floor/roof
            caseWallsPositions[i][0] = 0f;                   //x
            caseWallsPositions[i][1] = floorVsRoofPos;       //y
            caseWallsPositions[i][2] = standardZPos;         //z
            //adding position for left/right wall
            caseWallsPositions[++i][0] = leftVsRightWallPos; //x
            caseWallsPositions[i][1] =   vertPos;            //y
            caseWallsPositions[i][2] =   standardZPos;       //z
            //adding position for front/back wall
            caseWallsPositions[++i][0] = 0f;                 //x
            caseWallsPositions[i][1] =   vertPos;            //y
            caseWallsPositions[i][2] = frontVsBackWallPos;   //z

            floorVsRoofPos = vertPos+cylRad*1.5f+(wBoxHeight);
            leftVsRightWallPos = -leftVsRightWallPos;
            frontVsBackWallPos = disBetCyl*posOfLastCyl;
            i++;
        }
        //setting appearance
        Appearance woodStyle = new Appearance();
        TextureLoader loader = new TextureLoader("img/case.jfif", null);
        ImageComponent2D image = loader.getImage();

        Texture2D wood = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA, image.getWidth(), image.getHeight());
        wood.setImage(0, image);
        wood.setBoundaryModeS(Texture.WRAP);
        wood.setBoundaryModeT(Texture.WRAP);

        woodStyle.setTexture(wood);


        //adding walls in order: floor, roof, left, right, front, back
        for(int j = 0; j < 6; j++)
        {
                caseBoxes.add(new Box(caseDimensions[j][0], caseDimensions[j][1] , caseDimensions[j][2], woodStyle));

                Transform3D initPos = new Transform3D();
                initPos.set(new Vector3f( caseWallsPositions[j][0], caseWallsPositions[j][1], caseWallsPositions[j][2]));

                setBoxPos.add(new TransformGroup(initPos));
                setBoxPos.get(j).setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
                setBoxPos.get(j).addChild(caseBoxes.get(j));

                sceneBG.addChild(setBoxPos.get(j));
        }
    }


    public int randomInt()
    // Generator of numbers from 1 to 9
    {
        int i = (int)(Math.random()*10);
        i = (i == 0) ? randomInt() : i;

        return (i);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()){
            case KeyEvent.VK_LEFT:
                leftButton = true;
                break;
            case KeyEvent.VK_RIGHT:
                rightButton = true;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()){
            case KeyEvent.VK_LEFT:
                leftButton = false;
                break;
            case KeyEvent.VK_RIGHT:
                rightButton = false;
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == startButton)
            if(!clock1.isRunning())
                clock1.start();
            else if(e.getSource() == stopButton)
                if(clock1.isRunning())
                    clock1.stop();

        if(e.getSource() == setDefaultViewButton)
            initUserPosition();

        if(e.getSource() == hintButton)
        {
            boolean roofMoving = true;
            while(roofMoving)
            {

                Transform3D boxTrans = new Transform3D();
                boxTrans.setTranslation(new Vector3f(0f + dxRoof, vertPos+cylRad*1.5f+(wBoxHeight), (disBetCyl*(posOfFirstCyl+posOfLastCyl+0.5f)/2) - cylH/2));
                setBoxPos.get(3).setTransform(boxTrans);

                dxRoof += 0.1f;

                if (dxRoof >= cylRad*3f)
                    roofMoving = false;

            }
        }

        if(gameEnded == 0)
            if(whatCyl != numOfCyl || !nextCyl)         // in other words: if the user has not reached the situation
                setAngle();                             // when he turns the last cylinder, which is located in the correct position
            else
                gameEnded = 1;

        if(gameEnded == 1) {
            Transform3D boxTrans = new Transform3D();
            boxTrans.setTranslation(new Vector3f(0f, (vertPos+cylRad*1.5f+wBoxHeight)- dyWBox, (disBetCyl*(posOfFirstCyl+posOfLastCyl+0.5f)/2) - cylH/2));
            moveBox.setTransform(boxTrans);

            dyWBox += 0.1f;

            if (dyWBox >= cylRad*1.5f)
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

            if (clockwiseDir) {                             // if the user rotates to the right (so he can't rotates to the left)
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
            for(int i = 0; i < numOfCyl; i++)
                if(i % 2 == 0)
                    stepsKey.add(10-decodingKey.get(i));
                else
                    stepsKey.add(decodingKey.get(i));
        }
        else {                                              // otherwise
            for(int i = 0; i < numOfCyl; i++)
                if(i % 2 == 0)
                    stepsKey.add(decodingKey.get(i));
                else
                    stepsKey.add(10-decodingKey.get(i));
        }
    }


    private void rotateCyl()
    // Rotates the main and secondary cylinders
    {
        Transform3D rotMain = new Transform3D();
        rotMain.rotY(angleMain);
        rotCyl.get(0).setTransform(rotMain);

        Transform3D rot = new Transform3D();
        rot.rotY(angle);
        rotCyl.get(whatCyl).setTransform(rot);

        stepNum++;
    }


    private void checkKey()
    // Checks whether the user has taken as many steps as necessary for the cylinder to be in the correct position
    {
        if(whatCyl != 0 && whatCyl <= numOfCyl) {
            if(stepNum == stepsKey.get(whatCyl-1)) {
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

}
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import com.sun.j3d.audioengines.javasound.JavaSoundMixer;
import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.behaviors.vp.*;
import javax.media.j3d.*;
import javax.vecmath.*;


public class SafeUniverse extends JPanel implements ActionListener, KeyListener
{

    // Window parameters
    private static final int PWIDTH    = 512;   // size of panel
    private static final int PHEIGHT   = 512;
    private static final int BOUNDSIZE = 100;   // bounds larger than the world

    // Initial user position
    public static final Point3d USERPOS = new Point3d(13,10,13);

    // Canvas and world elements
    public Canvas3D canvas3D;
    public SimpleUniverse su;
    public BranchGroup sceneBG;
    public BoundingSphere bounds;
    public ViewingPlatform vp;
    public TransformGroup steerTG;
    public Timer clock;

    // Sound elements
    public String fileTick     = "tick.wav";
    public String fileTickNext = "tickNext.wav";
    public PointSound tick;
    public PointSound tickNext;

    // Panel and button elements
    public JPanel panel1;
    public JPanel panel2;
    public Button infoButton;
    public Button saveButton;
    public Choice levelChoice;
    public Button startButton;
    public Button pauseButton;
    public Button setDefViewButton;
    public Button hintButton;
    public boolean leftButton  = false;
    public boolean rightButton = false;
    public boolean addSave     = false;

    // Level and hint parameters
    public int whatHint = 0;
    public int numOfCylinders = 0;
    public boolean isHintLI;
    public boolean isHintGN;
    public boolean isHintSB;

    // Game implementation objects
    public SafeCreation sC;
    public SafeInteraction sI;
    public SafeSaving sS;


    public SafeUniverse()
    // A panel holding an initial 3D canvas
    {
        setLayout(new BorderLayout());
        setOpaque(false);
        setPreferredSize(new Dimension(PWIDTH, PHEIGHT));

        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        canvas3D = new Canvas3D(config);
        add("Center", canvas3D);
        canvas3D.addKeyListener(this);

        createButtons();                // adding buttons
        panel2.setVisible(false);

        canvas3D.setFocusable(true);
        canvas3D.requestFocus();

        clock = new Timer(10, this);
    }


    private void createSceneGraph()
    // Initializing the scene
    {
        bounds  = new BoundingSphere(new Point3d(0,0,0), BOUNDSIZE);
        sceneBG = new BranchGroup();
        sceneBG.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);

        lightScene();                                   // adding the lights
        addBackground();                                // adding the sky
        sceneBG.addChild(new SafePlatform().getBG());   // adding the floor

        // adding the sounds
        tick = new PointSound();
        tickNext = new PointSound();
        addSounds(tick, fileTick);
        addSounds(tickNext, fileTickNext);

        sceneBG.compile();      // fixing the scene
    }


    private void createButtons()
    // Adding 2 panels with buttons
    {
        // first panel
        infoButton  = new Button("Info");
        levelChoice = new Choice();
        startButton = new Button("Start New Game");

        levelChoice.add("Easy");
        levelChoice.add("Medium");
        levelChoice.add("Hard");
        levelChoice.select(levelChoice.getItem(whatHint));

        panel1 = new JPanel();
        panel1.add(infoButton);
        panel1.add(levelChoice);
        panel1.add(startButton);
        add(""+"North", panel1);

        infoButton.addActionListener(this);
        infoButton.addKeyListener(this);
        startButton.addActionListener(this);
        startButton.addKeyListener(this);

        // second panel
        pauseButton      = new Button("Pause/Continue");
        setDefViewButton = new Button("Default View");
        hintButton       = new Button("Hint");
        saveButton       = new Button("Save");

        panel2 = new JPanel();
        panel2.add(pauseButton);
        panel2.add(setDefViewButton);
        panel2.add(hintButton);
        add(""+"South", panel2);

        pauseButton.addActionListener(this);
        pauseButton.addKeyListener(this);
        setDefViewButton.addKeyListener(this);
        setDefViewButton.addActionListener(this);
        hintButton.addKeyListener(this);
        hintButton.addActionListener(this);
        saveButton.addActionListener(this);
        saveButton.addKeyListener(this);
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
        Vector3f light1Dir  = new Vector3f(-1.0f, -1.0f, -1.0f);  // left, down, backwards
        Vector3f light2Dir  = new Vector3f(1.0f, -1.0f, 1.0f);    // right, down, forwards

        DirectionalLight light1 = new DirectionalLight(white, light1Dir);
        light1.setInfluencingBounds(bounds);
        sceneBG.addChild(light1);

        DirectionalLight light2 = new DirectionalLight(white, light2Dir);
        light2.setInfluencingBounds(bounds);
        sceneBG.addChild(light2);
    }


    private void addBackground()
    // Adding a blue sky
    {
        Background back = new Background();
        back.setApplicationBounds(bounds);
        back.setColor(0.17f, 0.75f, 0.93f);    // sky colour

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


    private void orbitControls()
    // Allows the user to rotate around the scene, and to zoom in and out
    {
        OrbitBehavior orbit = new OrbitBehavior(canvas3D, OrbitBehavior.REVERSE_ALL);
        orbit.setSchedulingBounds(bounds);

        ViewingPlatform vp = su.getViewingPlatform();
        vp.setViewPlatformBehavior(orbit);
    }


    public void initUserPosition()
    // Setting the user's initial viewpoint using lookAt()
    {
        Transform3D t3d = new Transform3D();
        steerTG.getTransform(t3d);

        // args are: viewer position, where looking, up direction
        t3d.lookAt(USERPOS, new Point3d(0,2,0), new Vector3d(0,5,0));
        t3d.invert();

        steerTG.setTransform(t3d);
    }


    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
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
        switch (e.getKeyCode()) {
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
        Object action = e.getSource();

        // "Start New Game" button is clicked
        if(action == startButton) {
            // analysis of the selected level and creation of a new world
            initializeGame();

            // sending to sS current system time (required for saving)
            sS.setStartTime(System.currentTimeMillis());

            // timer start
            if(!clock.isRunning())
                clock.start();
        }

        // "Pause/Continue" button is clicked
        if(action == pauseButton)
            if(clock.isRunning())
                clock.stop();
            else
                clock.start();

        // "Info" button is clicked
        if(action == infoButton)
            sI.info(this);           // showing a window with information about the game

        // "Default View" button is clicked
        if(action == setDefViewButton)
            initUserPosition();             // setting up default view

        // "Save" button is clicked
        if(action == saveButton)
            sS.createSave();            // creating a save

        // "Hint" button is clicked
        if(action == hintButton && clock.isRunning()) {
            // hint depending on the difficulty level of the game
            switch(whatHint) {
                case 0:
                    isHintLI = true;
                    break;
                case 1:
                    isHintGN = true;
                    break;
                case 2:
                    isHintSB = true;
                    break;
            }

            // sending to sS information about use of hint (required for saving)
            sS.hintWasUsed();

            // removing hint button
            panel2.remove(hintButton);
            updateUI();
        }

        // easy level hint: user can look inside the box and see safe mechanism
        if(isHintLI)
            isHintLI = sI.lookInside();

        // medium level hint: user can see one necessary digit of the combination within a few seconds
        if(isHintGN)
            isHintGN = sI.giveDigit();

        // hard level hint: user only can take a step back
        if(isHintSB)
            isHintSB = sI.stepBack();

        // adding "Save" button if the game is completely over
        if(!addSave) {
            if(sI.gameEnded == 2) {
                panel2.add(saveButton);
                updateUI();

                addSave = true;
            }
        }

        // game process
        sI.safeGame(leftButton, rightButton, tick, tickNext, sC, sS);
    }


    public void initializeGame()
    // Removing old and creating new canvas. Creating universe with new objects
    {
        // setting parameters depending on difficulty level
        setLevel();

        // clearing JPanel
        removeAll();
        updateUI();

        // creating new canvas and universe
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        canvas3D = new Canvas3D(config);
        add("Center", canvas3D);
        canvas3D.addKeyListener(this);

        createButtons();            // adding buttons

        canvas3D.setFocusable(true);
        canvas3D.requestFocus();

        su = new SimpleUniverse(canvas3D);
        vp = su.getViewingPlatform();
        steerTG = vp.getViewPlatformTransform();

        createSceneGraph();         // initializing the scene
        initUserPosition();         // setting default user's viewpoint
        orbitControls();            // controlling the movement of the viewpoint

        // creating a sounds mixer to use our sounds with and initialising it
        JavaSoundMixer myMixer = new JavaSoundMixer(su.getViewer().getPhysicalEnvironment());
        myMixer.initialize();

        su.addBranchGraph(sceneBG);

        // creating new game implementation objects
        sS = new SafeSaving();
        sC = new SafeCreation(numOfCylinders, sS);
        sC.addElements(sceneBG);                        // adding cylinders, winning box and case

        sI = new SafeInteraction();
    }


    public void setLevel()
    // Setting number of cylinders and hint type depending on the selected difficulty level
    {
        switch(levelChoice.getSelectedItem()) {
            case "Easy":
                numOfCylinders = 5;
                whatHint = 0;
                break;
            case "Medium":
                numOfCylinders = 7;
                whatHint = 1;
                break;
            case "Hard":
                numOfCylinders = 9;
                whatHint = 2;
                break;
        }
    }

}
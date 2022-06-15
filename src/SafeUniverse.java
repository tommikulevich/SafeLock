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
// Holding the 3D canvas where the loaded elements are displayed
{
    private static final int PWIDTH = 512;      // size of panel
    private static final int PHEIGHT = 512;
    public static final int BOUNDSIZE = 100;   // larger than the world

    public static final Point3d USERPOS = new Point3d(13,10,13); // initial user position

    public Canvas3D canvas3D;
    public SimpleUniverse su;
    public BranchGroup sceneBG;
    public BoundingSphere bounds;
    public ViewingPlatform vp;
    public TransformGroup steerTG;

    public String fileTick = "tick.wav";
    public String fileTickNext = "tickNext.wav";
    public PointSound tick;
    public PointSound tickNext;

    public JPanel panel1;
    public JPanel panel2;

    public Timer clock;
    public boolean leftButton = false, rightButton = false;
    public Button infoButton;
    public Button saveButton;
    public Choice levelChoice;
    public Button startButton;
    public Button pauseButton;
    public Button setDefaultViewButton;
    public Button hintButton;

    public boolean isHint;
    public boolean isHintLI;
    public boolean isHintGN;
    public boolean isHintSB;
    public int whatHint = 0;
    public int numOfCylinders = 0;

    public SafeCreation sC;
    public SafeInteraction sI;
    public SafeSaving sS;

    public SafeUniverse()
    // A panel holding a 3D canvas
    {
        setLayout(new BorderLayout());
        setOpaque(false);
        setPreferredSize(new Dimension(PWIDTH, PHEIGHT));

        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        canvas3D = new Canvas3D(config);
        add("Center", canvas3D);
        canvas3D.addKeyListener(this);

        createButtons();
        panel2.setVisible(false);

        canvas3D.setFocusable(true);    // giving focus to the canvas
        canvas3D.requestFocus();

        clock = new Timer(10, this);
    }


    private void createSceneGraph()
    // Initializing the scene
    {
        sceneBG = new BranchGroup();
        sceneBG.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
        bounds = new BoundingSphere(new Point3d(0,0,0), BOUNDSIZE);

        lightScene();                                   // adding the lights
        addBackground();                                // adding the sky
        sceneBG.addChild(new SafePlatform().getBG());   // adding the floor

        tick = new PointSound();
        tickNext = new PointSound();
        addSounds(tick, fileTick);                      // adding the sounds
        addSounds(tickNext, fileTickNext);

        sceneBG.compile();      // fixing the scene
    }


    private void createButtons()
    // Adding buttons
    {
        infoButton = new Button("Info");
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

        pauseButton = new Button("Pause/Continue");
        setDefaultViewButton = new Button("Default View");
        hintButton = new Button("Hint");
        saveButton = new Button("Save");

        panel2 = new JPanel();
        panel2.add(pauseButton);
        panel2.add(setDefaultViewButton);
        panel2.add(hintButton);
        panel2.add(saveButton);

        add(""+"South", panel2);

        pauseButton.addActionListener(this);
        pauseButton.addKeyListener(this);
        setDefaultViewButton.addKeyListener(this);
        setDefaultViewButton.addActionListener(this);
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


    private void orbitControls(Canvas3D c)
    // OrbitBehaviour allows the user to rotate around the scene, and to zoom in and out
    {
        OrbitBehavior orbit = new OrbitBehavior(c, OrbitBehavior.REVERSE_ALL);
        orbit.setSchedulingBounds(bounds);

        ViewingPlatform vp = su.getViewingPlatform();
        vp.setViewPlatformBehavior(orbit);
    }


    public void initUserPosition()
    // Setting the user's initial viewpoint using lookAt()
    {
        Transform3D t3d = new Transform3D();
        steerTG.getTransform(t3d);

        // args are: viewer pos, where looking, up direction
        t3d.lookAt(USERPOS, new Point3d(0,2,0), new Vector3d(0,5,0));
        t3d.invert();

        steerTG.setTransform(t3d);
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
        Object action = e.getSource();

        if(action == startButton) {
            initializeGame();
            //sending to sS current system time
            sS.setStartTime(System.currentTimeMillis());
            isHint = false;

            if(!clock.isRunning())
                clock.start();
        }

        if(action == pauseButton)
            if(clock.isRunning())
                clock.stop();
            else
                clock.start();

        if(action == infoButton)
            sI.info(this);

        if(action == setDefaultViewButton)
            initUserPosition();

        if(action == saveButton)
            //creating save
            sS.createSave();

        if(action == hintButton && clock.isRunning() && !isHint) {
            switch(whatHint)
            {
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

            isHint = true;
        }

        if(isHintLI) {
            isHintLI = sI.lookInside();
            //sending to sS information about use of hint
            sS.hintWasUsed();
        }

        if(isHintGN) {
            isHintGN = sI.giveNum();
            //sending to sS information about use of hint
            sS.hintWasUsed();
        }

        if(isHintSB) {
            isHintSB = sI.stepBack();
            //sending to sS information about use of hint
            sS.hintWasUsed();
        }
        //sending sS object to SafeInteraction
        sI.safeGame(leftButton, rightButton, tick, tickNext, sC, sS);
    }

    public void initializeGame()
    //
    {
        setLevel();
        removeAll();
        updateUI();

        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        canvas3D = new Canvas3D(config);
        add("Center", canvas3D);
        canvas3D.addKeyListener(this);

        createButtons();

        canvas3D.setFocusable(true);    // giving focus to the canvas
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

        sS = new SafeSaving();
        //sending sS object to SafeCreation
        sC = new SafeCreation(numOfCylinders, sS);
        sC.addElements(sceneBG);                        // adding cylinders, winning box and case

        sI = new SafeInteraction();
    }


    public void setLevel()
    //
    {
        switch(levelChoice.getSelectedItem())
        {
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
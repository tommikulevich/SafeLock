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

    public static final Point3d USERPOS = new Point3d(0,5,20); // initial user position

    public SimpleUniverse su;
    public BranchGroup sceneBG;
    public BoundingSphere bounds;
    public ViewingPlatform vp;
    public TransformGroup steerTG;

    public String fileTick = "tick.wav";
    public String fileTickNext = "tickNext.wav";
    public PointSound tick = new PointSound();
    public PointSound tickNext = new PointSound();

    public JPanel panel1;
    public JPanel panel2;

    public Timer clock1;
    public boolean leftButton = false, rightButton = false;
    public Button infoButton;
    public Button historyButton;
    public Choice levelChoice;
    public Button startButton;
    public Button pauseButton;
    public Button setDefaultViewButton;
    public Button stepBackButton;
    public Button hintButton;

    boolean isHint;

    public SafeCreation sC;
    public SafeInteraction sI;


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

        createButtons();                   // adding buttons

        clock1 = new Timer(10, this);

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

        sC = new SafeCreation();
        sC.addElements(sceneBG);                        // adding cylinders, winning box and case

        sI = new SafeInteraction();

        sceneBG.compile();      // fixing the scene
    }


    private void createButtons()
    // Adding buttons
    {
        infoButton = new Button("Info");
        historyButton = new Button("History");
        levelChoice = new Choice();
        startButton = new Button("Start New Game");
        pauseButton = new Button("Pause");
        setDefaultViewButton = new Button("Default View");
        stepBackButton = new Button("Step Back");
        hintButton = new Button("Hint I");

        levelChoice.add("Easy");
        levelChoice.add("Medium");
        levelChoice.add("Hard");

        panel1 = new JPanel();
        panel1.add(infoButton);
        panel1.add(historyButton);
        panel1.add(startButton);
        panel1.add(levelChoice);
        add(""+"North", panel1);

        infoButton.addActionListener(this);
        infoButton.addKeyListener(this);
        historyButton.addActionListener(this);
        historyButton.addKeyListener(this);
        startButton.addActionListener(this);
        startButton.addKeyListener(this);
        // in fact levelChoice doesn't require actionListener
        // to get it actual value we can use levelChoice.getItem(levelChoice.getSelectedIndex());

        panel2 = new JPanel();
        panel2.add(pauseButton);
        panel2.add(setDefaultViewButton);
        panel2.add(stepBackButton);
        panel2.add(hintButton);
        add(""+"South", panel2);

        pauseButton.addActionListener(this);
        pauseButton.addKeyListener(this);
        setDefaultViewButton.addKeyListener(this);
        setDefaultViewButton.addActionListener(this);
        stepBackButton.addKeyListener(this);
        stepBackButton.addActionListener(this);
        hintButton.addKeyListener(this);
        hintButton.addActionListener(this);
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
        t3d.lookAt(USERPOS, new Point3d(0,2,5), new Vector3d(0,4,0));
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

        if(action == startButton)
            if(!clock1.isRunning())
                clock1.start();

        if(action == pauseButton)
            if(clock1.isRunning())
                clock1.stop();
            else
                clock1.start();

        if(action == infoButton)
            sI.info(this);

        if(action == setDefaultViewButton)
            initUserPosition();

        if(action == hintButton)
            if(clock1.isRunning())
                isHint = true;

        if(isHint)
            isHint = sI.hintFloor();

        if(action == stepBackButton)
            if(clock1.isRunning())
                sI.stepBack();

        sI.safeGame(leftButton, rightButton, tick, tickNext, sC);
    }
}
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.geometry.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import com.sun.j3d.utils.behaviors.vp.*;

// import com.tornadolabs.j3dtree.*;    // for displaying the scene graph


public class WrapCheckers3D extends JPanel implements ActionListener, KeyListener
// Holds the 3D canvas where the loaded image is displayed
{
    //parameters of cylinders
    int numOfCyl = 3;
    float disBetCyl = 1f;
    float vertPos = 3f;
    float cylRad = 1f;
    float cylH = 0.5f;
    float axRad = 0.1f;
    int posOfFirstCyl = 2;
    private Timer clock1;
    private boolean lefButton = false, rightButton = false;
    private Button startButton = new Button("Run");
    private Button stopButton = new Button("Stop");

    private static final int PWIDTH = 512;   // size of panel
    private static final int PHEIGHT = 512;

    private static final int BOUNDSIZE = 100;  // larger than world

    private static final Point3d USERPOSN = new Point3d(0,5,20); // initial user position

    private SimpleUniverse su;
    private BranchGroup sceneBG;
    private BoundingSphere bounds;   // for environment nodes

    // private Java3dTree j3dTree;   // frame to hold tree display
    public WrapCheckers3D()
    // A panel holding a 3D canvas: the usual way of linking Java 3D to Swing
    {
        setLayout(new BorderLayout());
        setOpaque(false);
        setPreferredSize(new Dimension(PWIDTH, PHEIGHT));

        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        Canvas3D canvas3D = new Canvas3D(config);
        add("Center", canvas3D);
        canvas3D.addKeyListener(this);
        clock1 = new Timer(10, this);
        JPanel panel = new JPanel();
        panel.add(startButton);
        panel.add(stopButton);
        add(""+ "West",panel);
        startButton.addActionListener(this);
        startButton.addKeyListener(this);

        canvas3D.setFocusable(true);     // give focus to the canvas
        canvas3D.requestFocus();
        su = new SimpleUniverse(canvas3D);

        // j3dTree = new Java3dTree();   // create a display tree for the SG

        createSceneGraph();
        initUserPosition();        // set user's viewpoint
        orbitControls(canvas3D);   // controls for moving the viewpoint
        su.addBranchGraph( sceneBG );
    }

    private void createSceneGraph()
    // initilize the scene
    {
        sceneBG = new BranchGroup();
        bounds = new BoundingSphere(new Point3d(0,0,0), BOUNDSIZE);

        lightScene();         // add the lights
        addBackground();      // add the sky
        sceneBG.addChild(new CheckerFloor().getBG());  // add the floor

        floatingCylinders();     // add the floating sphere

        // j3dTree.recursiveApplyCapability( sceneBG );   // set capabilities for tree display

        sceneBG.compile();   // fix the scene
    }


    private void lightScene()
    /* One ambient light, 2 directional lights */
    {
        Color3f white = new Color3f(1.0f, 1.0f, 1.0f);

        // Set up the ambient light
        AmbientLight ambientLightNode = new AmbientLight(white);
        ambientLightNode.setInfluencingBounds(bounds);
        sceneBG.addChild(ambientLightNode);

        // Set up the directional lights
        Vector3f light1Direction  = new Vector3f(-1.0f, -1.0f, -1.0f); // left, down, backwards
        Vector3f light2Direction  = new Vector3f(1.0f, -1.0f, 1.0f); // right, down, forwards

        DirectionalLight light1 = new DirectionalLight(white, light1Direction);
        light1.setInfluencingBounds(bounds);
        sceneBG.addChild(light1);

        DirectionalLight light2 = new DirectionalLight(white, light2Direction);
        light2.setInfluencingBounds(bounds);
        sceneBG.addChild(light2);
    }


    private void addBackground()
    // A blue sky
    {
        Background back = new Background();
        back.setApplicationBounds(bounds);
        back.setColor(0.17f, 0.65f, 0.92f);    // sky colour
        sceneBG.addChild(back);
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
    // Set the user's initial viewpoint using lookAt()
    {
      ViewingPlatform vp = su.getViewingPlatform();
      TransformGroup steerTG = vp.getViewPlatformTransform();

      Transform3D t3d = new Transform3D();
      steerTG.getTransform(t3d);

      // args are: viewer posn, where looking, up direction
      t3d.lookAt(USERPOSN, new Point3d(0,0,0), new Vector3d(0,4,0));
      t3d.invert();

      steerTG.setTransform(t3d);
    }


    // ---------------------- floating cylinders -----------------

    public void floatingCylinders()
    {
        // Create the blue appearance node
        Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
        Color3f blue = new Color3f(0.3f, 0.3f, 0.8f);
        Color3f specular = new Color3f(0.9f, 0.9f, 0.9f);

        Material blueMat= new Material(blue, black, blue, specular, 25.0f);
        // sets ambient, emissive, diffuse, specular, shininess
        blueMat.setLightingEnable(true);

        Appearance blueApp = new Appearance();
        blueApp.setMaterial(blueMat);

        //creating Cylinders
        ArrayList<Cylinder>cylinders = new ArrayList<Cylinder>();
        //setting params of cylinders
        for(int i = 0; i < numOfCyl; i++){
            cylinders.add(new Cylinder(cylRad, cylH, blueApp));
        }
        //creating transformations of position for cylinders
        ArrayList<Transform3D>posCyl = new ArrayList<Transform3D>();
        //setting this transformation by position algorithm
        int posOfLastCyl = posOfFirstCyl;
        for (int i = 0; i < numOfCyl; i++, posOfLastCyl--){
            Transform3D k = new Transform3D();
            k.set(new Vector3f(0,disBetCyl*posOfLastCyl,-vertPos));
            posCyl.add(k);
        }
        //setting parameters for axis by axis algorithm
        cylinders.add(new Cylinder(axRad, disBetCyl*(posOfFirstCyl-posOfLastCyl), blueApp));
        //creating and setting position transformation for axis
        Transform3D p = new Transform3D();
        p.set(new Vector3f(0, disBetCyl*(posOfFirstCyl+posOfLastCyl)/2,-vertPos));
        posCyl.add(p);
        //creating transformation group and matching with transformation
        ArrayList<TransformGroup>tg = new ArrayList<TransformGroup>();
        for (int i = 0; i < numOfCyl+1; i++){
            TransformGroup k = new TransformGroup(posCyl.get(i));
            tg.add(k);
        }
        //rotation transformation
        Transform3D tmp_rot = new Transform3D();
        tmp_rot.rotX(Math.PI/2); //Math.PI/2
        TransformGroup tg_rot = new TransformGroup(tmp_rot);
        //matching transformation groups with rotation
        for (int i = 0; i < numOfCyl+1; i++){
            tg.get(i).addChild(cylinders.get(i));
            tg_rot.addChild(tg.get(i));
        }
        //adding rotation to scene
        sceneBG.addChild(tg_rot);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==startButton){
            if(!clock1.isRunning()){
                clock1.start();
            }
        } else {
            if(lefButton == true) {
                numOfCyl++; //tutaj parametry z oborotu nałem cylidndry bo żeby coś było
            }
            if(rightButton == true) {
                numOfCyl--; //tutaj parametry z oborotu nałem cylidndry bo żeby coś było
            }
            //tutaj transformacje obortu dodać
            //setTransform
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()){
            case KeyEvent.VK_LEFT:
                lefButton = true;
            case KeyEvent.VK_RIGHT:
                rightButton = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()){
            case KeyEvent.VK_LEFT:
                lefButton = false;
            case KeyEvent.VK_RIGHT:
                rightButton = false;
        }
    }
}

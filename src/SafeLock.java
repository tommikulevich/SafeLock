import javax.swing.JFrame;
import javax.media.j3d.*;
import javax.vecmath.*;
import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.behaviors.vp.*;


public class SafeLock extends JFrame
{

    public Canvas3D myCanvas3D;

    public SafeLock() {

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        myCanvas3D = new Canvas3D(SimpleUniverse.getPreferredConfiguration());

        SimpleUniverse simpUniv = new SimpleUniverse(myCanvas3D);
        simpUniv.getViewingPlatform().setNominalViewingTransform();

        createSceneGraph(simpUniv);

        addLight(simpUniv);

        OrbitBehavior ob = new OrbitBehavior(myCanvas3D);
        ob.setSchedulingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), Double.MAX_VALUE));
        simpUniv.getViewingPlatform().setViewPlatformBehavior(ob);

        setTitle("Mechanizm dzialania sejfu");
        setSize(700, 700);
        getContentPane().add("Center", myCanvas3D);
        setVisible(true);

    }


    public static void main(String[] args) {
        SafeLock safeLock = new SafeLock();
    }


    public void createSceneGraph(SimpleUniverse su)
    {

        Appearance redApp = new Appearance();
        setToMyDefaultAppearance(redApp,new Color3f(0.8f,0.0f,0.0f));

        float platformSize = 0.1f;

        Box platform = new Box(platformSize,platformSize,platformSize,redApp);
        Transform3D tfPlatform = new Transform3D();
        tfPlatform.rotY(Math.PI/6);

        TransformGroup tgPlatform = new TransformGroup(tfPlatform);
        tgPlatform.addChild(platform);


        Appearance greenApp = new Appearance();
        setToMyDefaultAppearance(greenApp,new Color3f(0.0f,0.7f,0.0f));

        float cabinRadius = 0.1f;

        Sphere cabin = new Sphere(cabinRadius,greenApp);

        TransformGroup tgCabin = new TransformGroup();
        tgCabin.addChild(cabin);


        Appearance blueApp = new Appearance();
        setToMyDefaultAppearance(blueApp,new Color3f(0.0f,0.0f,1.0f));

        Box rotor = new Box(0.4f,0.0001f,0.01f,blueApp);

        TransformGroup tgmRotor = new TransformGroup();
        tgmRotor.addChild(rotor);

        Transform3D bladeRotationAxis = new Transform3D();
        int timeStartRotor = 2000;
        int noStartRotations = 2;
        int timeSlowRotation = 1500;


        Alpha bladeRotationStartAlpha = new Alpha(noStartRotations,
                Alpha.INCREASING_ENABLE,
                timeStartRotor,
                0,timeSlowRotation,0,0,0,0,0);

        RotationInterpolator bladeRotationStart = new RotationInterpolator(
                bladeRotationStartAlpha,tgmRotor,
                bladeRotationAxis,0.0f,(float) Math.PI*2);

        BoundingSphere bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0),Double.MAX_VALUE);
        bladeRotationStart.setSchedulingBounds(bounds);


        int timeFastRotation = 500;
        int timeOneWayFlight = 2000;
        int timeHovering = 1000;
        int timeStartWait = 1000;

        int timeFlightStart = timeStartRotor+timeSlowRotation*noStartRotations+timeStartWait;
        int noFastRotations = 1+ ((timeStartWait+2*timeOneWayFlight+timeHovering)/timeFastRotation);


        Alpha bladeRotationAlpha = new Alpha(noFastRotations,Alpha.INCREASING_ENABLE,
                timeStartRotor+timeSlowRotation*noStartRotations,
                0,timeFastRotation,0,0,0,0,0);

        RotationInterpolator bladeRotation = new RotationInterpolator(
                bladeRotationAlpha,tgmRotor,
                bladeRotationAxis,0.0f,(float) Math.PI*2);

        bladeRotation.setSchedulingBounds(bounds);


        tgmRotor.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tgmRotor.addChild(bladeRotationStart);
        tgmRotor.addChild(bladeRotation);


        Transform3D tfRotor = new Transform3D();
        tfRotor.setTranslation(new Vector3f(0.0f,cabinRadius,0.0f));
        TransformGroup tgRotor = new TransformGroup(tfRotor);
        tgRotor.addChild(tgmRotor);


        float halfTailLength = 0.2f;

        Box tail = new Box(halfTailLength,0.02f,0.02f,greenApp);

        Transform3D tfTail = new Transform3D();
        tfTail.setTranslation(new Vector3f(cabinRadius+halfTailLength,0.0f,0.0f));

        TransformGroup tgTail = new TransformGroup(tfTail);
        tgTail.addChild(tail);


        float BoxLength1 = 0.03f;
        float BoxLength2 = 0.02f;
        float BoxLength3 = 0.01f;
        float BoxHeight = 0.01f;

        Box GroundHeck = new Box(BoxLength1,BoxHeight,0.01f,greenApp);
        Box MiddleHeck = new Box(BoxLength2,BoxHeight,0.01f,greenApp);
        Box TopHeck = new Box(BoxLength3,BoxHeight,0.01f,greenApp);


        Transform3D tfGroundHeck = new Transform3D();
        tfGroundHeck.setTranslation(new Vector3f(0.0f,0.0f,0.0f));
        Transform3D tfMiddleHeck = new Transform3D();
        tfMiddleHeck.setTranslation(new Vector3f(BoxLength1-BoxLength2,2*BoxHeight,0.0f));
        Transform3D tfTopHeck = new Transform3D();
        tfTopHeck.setTranslation(new Vector3f(BoxLength1-BoxLength3,4*BoxHeight,0.0f));


        TransformGroup tgGroundHeck = new TransformGroup(tfGroundHeck);
        tgGroundHeck.addChild(GroundHeck);
        TransformGroup tgMiddleHeck = new TransformGroup(tfMiddleHeck);
        tgMiddleHeck.addChild(MiddleHeck);
        TransformGroup tgTopHeck = new TransformGroup(tfTopHeck);
        tgTopHeck.addChild(TopHeck);


        Transform3D tfHeck = new Transform3D();
        tfHeck.setTranslation(new Vector3f(cabinRadius+2*halfTailLength-BoxLength1,3*BoxHeight,0.0f));


        TransformGroup tgHeck = new TransformGroup(tfHeck);
        tgHeck.addChild(tgGroundHeck);
        tgHeck.addChild(tgMiddleHeck);
        tgHeck.addChild(tgTopHeck);


        float HeckRotorLength = 0.1f;

        Box HeckRotor = new Box(HeckRotorLength,0.01f,0.0001f,blueApp);

        TransformGroup tgmHeckRotor = new TransformGroup();
        tgmHeckRotor.addChild(HeckRotor);


        Transform3D bladeHeckRotationAxis = new Transform3D();
        bladeHeckRotationAxis.rotX(Math.PI/2);
        int timeStartHeckRotor = 2000;
        int noStartHeckRotations = 7;
        int timeSlowHeckRotation = 1500;


        Alpha bladeHeckRotationStartAlpha = new Alpha(noStartHeckRotations,
                Alpha.INCREASING_ENABLE,
                timeStartHeckRotor,
                0,timeSlowHeckRotation,0,0,0,0,0);

        RotationInterpolator bladeHeckRotationStart = new RotationInterpolator(
                bladeHeckRotationStartAlpha,tgmHeckRotor,
                bladeHeckRotationAxis,0.0f,(float) Math.PI*2);

        BoundingSphere Heckbounds = new BoundingSphere(new Point3d(0.0,0.0,0.0),Double.MAX_VALUE);
        bladeHeckRotationStart.setSchedulingBounds(Heckbounds);

        tgmHeckRotor.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tgmHeckRotor.addChild(bladeHeckRotationStart);

        Transform3D tfHeckRotor = new Transform3D();
        tfHeckRotor.setTranslation(new Vector3f(cabinRadius+2*halfTailLength-BoxLength1,0.0f,0.02f));
        TransformGroup tgHeckRotor = new TransformGroup(tfHeckRotor);
        tgHeckRotor.addChild(tgmHeckRotor);


        TransformGroup tgmHelicopter = new TransformGroup();

        tgmHelicopter.addChild(tgCabin);
        tgmHelicopter.addChild(tgRotor);
        tgmHelicopter.addChild(tgTail);
        tgmHelicopter.addChild(tgHeck);
        tgmHelicopter.addChild(tgHeckRotor);


        int timeAcc = 300;

        Transform3D helicopterFlightAxis = new Transform3D();
        helicopterFlightAxis.rotZ(0.4*Math.PI);

        Alpha helicopterAlpha = new Alpha(1,Alpha.INCREASING_ENABLE+Alpha.DECREASING_ENABLE,
                timeFlightStart,0,timeOneWayFlight,timeAcc,
                timeHovering,timeOneWayFlight,timeAcc,0);

        PositionInterpolator posIPHelicopter = new PositionInterpolator(helicopterAlpha,
                tgmHelicopter,helicopterFlightAxis,
                0.0f,0.5f);

        posIPHelicopter.setSchedulingBounds(bounds);

        tgmHelicopter.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tgmHelicopter.addChild(posIPHelicopter);


        Transform3D tfHelicopter = new Transform3D();
        tfHelicopter.setTranslation(new Vector3f(0.0f,platformSize+cabinRadius,0.0f));
        TransformGroup tgHelicopter = new TransformGroup(tfHelicopter);
        tgHelicopter.addChild(tgmHelicopter);


        Transform3D tfHeliPlat = new Transform3D();
        tfHeliPlat.setTranslation(new Vector3f(0.0f,0.1f,0.0f));
        TransformGroup tgHeliPlat = new TransformGroup(tfHeliPlat);
        tgHeliPlat.addChild(tgHelicopter);
        tgHeliPlat.addChild(tgPlatform);


        Appearance brownApp = new Appearance();
        setToMyDefaultAppearance(brownApp,new Color3f(0.5f,0.2f,0.2f));

        float trunkHeight = 0.4f;

        Cylinder trunk = new Cylinder(0.05f,trunkHeight,brownApp);

        TransformGroup tgTrunk = new TransformGroup();
        tgTrunk.addChild(trunk);


        float leavesHeight = 0.4f;

        Cone leaves = new Cone(0.3f,leavesHeight,greenApp);

        Transform3D tfLeaves = new Transform3D();
        tfLeaves.setTranslation(new Vector3f(0.0f,(trunkHeight+leavesHeight)/2,0.0f));

        TransformGroup tgLeaves = new TransformGroup(tfLeaves);
        tgLeaves.addChild(leaves);


        Transform3D tfTree = new Transform3D();
        tfTree.setTranslation(new Vector3f(-0.6f,0.0f,0.0f));

        TransformGroup tgTree = new TransformGroup(tfTree);
        tgTree.addChild(tgTrunk);
        tgTree.addChild(tgLeaves);


        BranchGroup theScene = new BranchGroup();

        theScene.addChild(tgHeliPlat);
        theScene.addChild(tgTree);

        theScene.compile();

        su.addBranchGraph(theScene);

    }


    public static void setToMyDefaultAppearance(Appearance app, Color3f col) {
        app.setMaterial(new Material(col,col,col,col,150.0f));
    }


    public void addLight(SimpleUniverse su)
    {

        BranchGroup bgLight = new BranchGroup();

        BoundingSphere bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);
        Color3f lightColour1 = new Color3f(1.0f,1.0f,1.0f);
        Vector3f lightDir1  = new Vector3f(-1.0f,0.0f,-0.5f);
        DirectionalLight light1 = new DirectionalLight(lightColour1, lightDir1);
        light1.setInfluencingBounds(bounds);

        bgLight.addChild(light1);
        su.addBranchGraph(bgLight);

    }


}
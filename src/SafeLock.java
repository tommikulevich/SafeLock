import java.awt.*;
import javax.swing.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.geometry.*;


public final class SafeLock extends JFrame{

    SafeLock(){
        
        super("Mechanizm dzialania sejfu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();

        Canvas3D canvas3D = new Canvas3D(config);
        canvas3D.setPreferredSize(new Dimension(800, 600));

        add(canvas3D);
        pack();
        setVisible(true);

        BranchGroup scene = createScene();
	    scene.compile();

        SimpleUniverse simpleU = new SimpleUniverse(canvas3D);

        Transform3D observerPos = new Transform3D();
        observerPos.set(new Vector3f(0.0f, 0.0f, 4.0f));

        simpleU.getViewingPlatform().getViewPlatformTransform().setTransform(observerPos);

        simpleU.addBranchGraph(scene);

    }

    BranchGroup createScene(){
        
        BranchGroup rootScene = new BranchGroup();
        Alpha alpha = new Alpha(-1, 5000);
        BoundingSphere bounds = new BoundingSphere();
        
        // Swiatla
        
        AmbientLight lightA = new AmbientLight();
        lightA.setInfluencingBounds(bounds);
        rootScene.addChild(lightA);

        DirectionalLight lightD = new DirectionalLight();
        lightD.setInfluencingBounds(bounds);
        lightD.setDirection(new Vector3f(0.0f, 0.0f, -1.0f));
        lightD.setColor(new Color3f(1.0f, 1.0f, 1.0f));
        rootScene.addChild(lightD);

        // Kula
        
        Appearance  ballStyle = new Appearance();
        ballStyle.setColoringAttributes(new ColoringAttributes(0.1f, 0.8f, 0.5f, ColoringAttributes.NICEST));

        Sphere ball = new Sphere(0.1f, ballStyle);
        rootScene.addChild(ball);


        return rootScene;

    }

    public static void main(String[] args){
        SafeLock safeLock = new SafeLock();
    }

}



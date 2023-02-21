import java.awt.*;
import javax.media.j3d.*;
import com.sun.j3d.utils.geometry.Text2D;
import javax.vecmath.*;
import java.util.ArrayList;


public class SafePlatform
{

    private final static int FLOOR_LEN = 20;  // should be even

    // Own colours for floor
    private final static Color3f blue = new Color3f(0.0f, 0.1f, 0.4f);
    private final static Color3f green = new Color3f(0.0f, 0.5f, 0.1f);
    private final static Color3f medRed = new Color3f(0.8f, 0.4f, 0.3f);
    private final static Color3f white = new Color3f(1.0f, 1.0f, 1.0f);

    private final BranchGroup floorBG;


    public SafePlatform()
    // Creating tiles, adding origin marker, then the axes labels
    {
        ArrayList blueCoords = new ArrayList();
        ArrayList greenCoords = new ArrayList();
        floorBG = new BranchGroup();

        boolean isBlue;

        for(int z = -FLOOR_LEN/2; z <= (FLOOR_LEN/2)-1; z++) {
            isBlue = z % 2 == 0;    // set colour for new row

            for(int x = -FLOOR_LEN/2; x <= (FLOOR_LEN/2)-1; x++) {
                if (isBlue)
                    createCoords(x, z, blueCoords);
                else
                    createCoords(x, z, greenCoords);

                isBlue = !isBlue;
            }
        }

        floorBG.addChild(new SafePlatformInit(blueCoords, blue));
        floorBG.addChild(new SafePlatformInit(greenCoords, green));

        addOriginMarker();
        labelAxes();
    }


    private void createCoords(int x, int z, ArrayList coords)
    // Coords for a single blue or green square, its left hand corner at (x,0,z)
    {
        // points created in counter-clockwise order
        Point3f p1 = new Point3f(x, 0.0f, z+1.0f);
        Point3f p2 = new Point3f(x+1.0f, 0.0f, z+1.0f);
        Point3f p3 = new Point3f(x+1.0f, 0.0f, z);
        Point3f p4 = new Point3f(x, 0.0f, z);

        coords.add(p1); coords.add(p2);
        coords.add(p3); coords.add(p4);
    }


    private void addOriginMarker()
    // A red square centered at (0,0,0), of length 0.5
    {
        // points created counter-clockwise, a bit above the floor
        Point3f p1 = new Point3f(-0.25f, 0.01f, 0.25f);
        Point3f p2 = new Point3f(0.25f, 0.01f, 0.25f);
        Point3f p3 = new Point3f(0.25f, 0.01f, -0.25f);
        Point3f p4 = new Point3f(-0.25f, 0.01f, -0.25f);

        ArrayList oCoords = new ArrayList();
        oCoords.add(p1); oCoords.add(p2);
        oCoords.add(p3); oCoords.add(p4);

        floorBG.addChild(new SafePlatformInit(oCoords, medRed));
    }


    private void labelAxes()
    // Placing numbers along the X- and Z-axes at the integer positions
    {
        Vector3d pt = new Vector3d();

        for (int i = -FLOOR_LEN/2; i <= FLOOR_LEN/2; i++) {
            pt.x = i;
            floorBG.addChild(makeText(pt,""+i) );   // along x-axis
        }

        pt.x = 0;
        for (int i = -FLOOR_LEN/2; i <= FLOOR_LEN/2; i++) {
            pt.z = i;
            floorBG.addChild(makeText(pt,""+i));    // along z-axis
        }
    }

    private TransformGroup makeText(Vector3d vertex, String text)
    // Creating a Text2D object at the specified vertex
    {
        Text2D message = new Text2D(text, white, "SansSerif", 36, Font.BOLD );

        TransformGroup tg = new TransformGroup();
        Transform3D t3d = new Transform3D();
        t3d.setTranslation(vertex);
        tg.setTransform(t3d);
        tg.addChild(message);

        return tg;
    }


    public BranchGroup getBG() {
        return floorBG;
    }

}

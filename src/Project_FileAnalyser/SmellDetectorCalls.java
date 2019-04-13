package Project_FileAnalyser;
import BloatCheckers.BloatedCodeAbuseCheck;
import ObjectOrientedAbusers.DataHiding;
import ObjectOrientedAbusers.SwitchChecker;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import utility_classes.ClassThreatLevels;
import utility_classes.CompilationUnitVisitor;
import utility_classes.ThreatLevel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class SmellDetectorCalls {
    private static SmellDetectorCalls INSTANCE = null;
    //store the class and its corresponding threat levels in a hashmap
    private HashMap<ClassOrInterfaceDeclaration, ClassThreatLevels> detections = new HashMap<>();
    private List<CompilationUnit> units;

    private SmellDetectorCalls(List<CompilationUnit> units) {
        this.units = units;
    }

    public static SmellDetectorCalls getInstance(){
        if(INSTANCE == null){
            throw new AssertionError("Initialize class first");
        }
        return INSTANCE;
    }

    public synchronized static SmellDetectorCalls init(List<CompilationUnit> units){
        if (INSTANCE != null)
        {
            // in my opinion this is optional, but for the purists it ensures
            // that you only ever get the same instance when you call getInstance
            throw new AssertionError("You already initialized me");
        }

        INSTANCE = new SmellDetectorCalls(units);
        return INSTANCE;
    }
    //function below is where you call on the different classes
    public void AnalyseProject() {

        //loop through all the different compilation units and create a list of their classes
        for (CompilationUnit cu : units) {
            List<ClassOrInterfaceDeclaration> classes = new ArrayList<>();
            CompilationUnitVisitor compunitvisitor = new CompilationUnitVisitor();
            compunitvisitor.visit(cu, classes);
            //preform the various checks
            BloatedCodeAbuseCheck checkBloats = new BloatedCodeAbuseCheck(classes);
            SwitchChecker switchC = new SwitchChecker();
            checkBloats.performBloatChecks();
            HashMap<ClassOrInterfaceDeclaration, HashMap> map = checkBloats.getClassThreats();

            /* this for loop is only needed by the bloatAbuse check
            *  it loops through every key in our hashmap "map"
            *  it then loops through the inner hashmap that is the value of each key in "map"
            *  this is where the blaoted threat level is stored.
            *  we then put the class and its ClassThreatLevels into the detections hashmap
            */
            for (ClassOrInterfaceDeclaration cl : map.keySet()) {
                HashMap value = map.get(cl);
                Set<ThreatLevel> t = value.keySet();
                for (ThreatLevel tl : t) {
                    //place the class name and all its threats in to the hashmap
                    getDetections().put(cl,new ClassThreatLevels(tl,switchC.complexityOfClass(cl)));
                }
            }
        }
    }
//not used yet but will in future.
    public HashMap getAnalysisResults(){
        return getDetections();
    }


    public void printResults(){
        for (ClassOrInterfaceDeclaration cl : detections.keySet()) {
         ClassThreatLevels value = detections.get(cl);
         System.out.println("\nCLASS:  "+cl.getName());
         System.out.println("Bloatedness: "+value.getBloatThreatLevel());
         System.out.println("Complexity: "+value.getOOAbuseThreatLevel());
            DataHiding dh = new DataHiding(cl);
            System.out.println("Data Hiding [returns objecs]: "+dh.isReturnObjects());
            System.out.println("Data Hiding [encapsulation]: "+dh.isSufficientPrivatisation());
        }
    }


    public HashMap<ClassOrInterfaceDeclaration, ClassThreatLevels> getDetections() {
        return detections;
    }

    public void setDetections(HashMap<ClassOrInterfaceDeclaration, ClassThreatLevels> detections) {
        this.detections = detections;
    }
}
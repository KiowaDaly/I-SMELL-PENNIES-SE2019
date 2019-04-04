package BloatCheckers;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.util.HashMap;
import java.util.List;

public class BloatedCodeAbuseCheck {
    private enum ThreatLevel{
        NONE,LOW,MEDIUM,HIGH
    }
    private final int METHOD_THRESHOLD = 15;
    private final double COMMENT_THRESHOLD = 0.5;
    private final int CLASS_LENGTH_THRESHOLD = 200;
    private final int VARIABLE_THRESHOLD = 20;
    final int METHOD_LINE_THRESHOLD = 30;
    final int PARAMETER_THRESHOLD = 5;
    private ClassBloatChecks check_bloat = new ClassBloatChecks();
    private MethodBloatChecks method_bloat = new MethodBloatChecks();
    private List<ClassOrInterfaceDeclaration> classes;

    private HashMap<ClassOrInterfaceDeclaration,HashMap> classThreats = new HashMap<>();

    private HashMap<MethodDeclaration,HashMap> MethodThreats = new HashMap<>();

    public BloatedCodeAbuseCheck(List<ClassOrInterfaceDeclaration> classes){
            this.classes = classes;
    }

    public void performBloatChecks(){
        for(ClassOrInterfaceDeclaration cl:classes){
            classThreats.put(cl,CheckClass(cl));
        }
    }

    private HashMap CheckClass(ClassOrInterfaceDeclaration n){
            int threatValue = 0;
            int classLength = check_bloat.getNumLines(n);
            threatValue += (classLength > CLASS_LENGTH_THRESHOLD)?1:0;
            threatValue += ((classLength-check_bloat.getNumComments(n))/classLength > COMMENT_THRESHOLD) ? 1:0;
            threatValue += (check_bloat.getNumFieldsOrVariables(n) > VARIABLE_THRESHOLD) ? 1:0;
            threatValue += (check_bloat.getNumMethods(n) > METHOD_THRESHOLD) ? 1:0;
            for (MethodDeclaration m:n.getMethods()) {
                MethodThreats.put(m,CheckMethod(m));
            }
            HashMap<ThreatLevel,Integer> threatPercentageHelper = new HashMap<>();
            int temp = threatValue;
            threatValue = (threatValue > 3) ? 3:threatValue;
            threatPercentageHelper.put(ThreatLevel.values()[threatValue],temp);
            return threatPercentageHelper;
    }

    private HashMap CheckMethod(MethodDeclaration m){

            int threatValue  = 0;
            int methodLines = method_bloat.getNumLines(m);
            threatValue += (methodLines > METHOD_LINE_THRESHOLD) ? 1:0;
            threatValue += ((methodLines - method_bloat.getNumComments(m))/100 > COMMENT_THRESHOLD) ? 1:0;
            threatValue += (method_bloat.getNumParameters(m) > PARAMETER_THRESHOLD) ? 1:0;
            threatValue += (method_bloat.getNumFieldsOrVariables(m) > VARIABLE_THRESHOLD) ? 1:0;
            HashMap<ThreatLevel,Integer> threatPercentageHelper = new HashMap<>();
            int temp = threatValue;
            threatValue = (threatValue > 3) ? 3:threatValue;
            threatPercentageHelper.put(ThreatLevel.values()[threatValue],temp);
            return threatPercentageHelper;
    }

    public HashMap getMethodThreats(){
            return MethodThreats;
    }
    public HashMap getClassThreats(){
            return classThreats;
    }

}

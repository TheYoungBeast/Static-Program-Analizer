package queryprocessor.evaluator;

import pkb.ast.abstraction.ASTNode;
import queryprocessor.preprocessor.synonyms.Synonym;
import utils.Pair;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public class PatternRelationships extends HashMap<Pair<Synonym<?>, Synonym<?>>, LinkedHashSet<Pair<ASTNode, ASTNode>>>
{
}

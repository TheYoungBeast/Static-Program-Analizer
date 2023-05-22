package queryprocessor.evaluator;

import pkb.ast.abstraction.ASTNode;
import queryprocessor.preprocessor.synonyms.Synonym;
import utils.Pair;

import java.util.HashMap;
import java.util.LinkedHashSet;

public class EntityRelationships extends HashMap<Pair<Synonym<?>, Synonym<?>>, LinkedHashSet<Pair<ASTNode, ASTNode>>>
{
}

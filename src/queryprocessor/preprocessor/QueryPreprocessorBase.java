package queryprocessor.preprocessor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import queryprocessor.preprocessor.exceptions.InvalidQueryException;
import queryprocessor.preprocessor.exceptions.MissingArgumentException;
import queryprocessor.querytree.*;

public class QueryPreprocessorBase implements QueryPreprocessor {

  public final List<String> keywords;

  public final HashSet<Synonym> synonymsList;

  private static final String selectRegex = "select(\\s*)[a-zA-Z]+[0-9]*(\\s*)(,*(\\s*)[a-zA-Z]*[0-9]*)*";

  public QueryPreprocessorBase() {
    this.synonymsList = new HashSet<>();
    this.keywords = Arrays.stream(Keyword.values())
        .map(k -> k.getPattern())
        .collect(Collectors.toList());
  }

  public QueryTree parseQuery(String query) throws InvalidQueryException, MissingArgumentException {
      if (query.length() == 0) {
          throw new InvalidQueryException("Empty query");
      }

    var array = query.split(";"); // positive look behind - query.split("((?<=;))");

    return parseQuery(array);
  }

  public QueryTree parseQuery(String[] querylines)
      throws InvalidQueryException, MissingArgumentException {
      if (querylines.length == 0) {
          throw new InvalidQueryException("The query not properly ended. ';' is missing", 1);
      }

    var tree = new QTree();

    boolean selectClause = false;
    for (int i = 0; i < querylines.length; i++) {
      var line = querylines[i];

        if (line.length() == 0) {
            continue;
        }

      // Does the current query's line contain a select clause?
      var pattern = Pattern.compile(selectRegex, Pattern.CASE_INSENSITIVE);

      if (pattern.matcher(line).find()) {
          if (selectClause) {
              throw new InvalidQueryException("Multiple select clauses", i, line);
          } else {
              selectClause = true;
          }
      }

      // Synonyms declarations
      if (!selectClause) {
        Keyword type = null;
        int start = 0;

        var set = new HashSet<Keyword>(
            Arrays.asList(
                Keyword.WHILE,
                Keyword.ASSIGN,
                Keyword.STATEMENT)
        );

        int cnt = 0;
        for (Keyword keyword : set) {
          var matcher = Pattern.compile(Pattern.quote(keyword.getPattern()),
              Pattern.CASE_INSENSITIVE).matcher(line);

          if (matcher.find()) {
            type = keyword;
            start = matcher.end();
            cnt++;
          }
        }

          if (cnt > 1) {
              throw new InvalidQueryException("Multiple synonym types in one declaration", i, line);
          }

          if (type == null) {
              throw new InvalidQueryException("Unrecognized synonym definition", i, line);
          }

        var synonyms = Pattern.compile(Keyword.SYNONYM.getPattern())
            .matcher(line.substring(start+1))
            .results()
            .map(mr -> mr.group())
            .collect(Collectors.toList());

          if (synonyms.isEmpty()) {
              throw new InvalidQueryException("Missing synonym identifier", i, line);
          }

        for (String synonym : synonyms) {
          var s = new Synonym(synonym, type);
            if (synonymsList.contains(s)) {
                throw new InvalidQueryException("Synonym already declared", i, synonym);
            } else {
                synonymsList.add(s);
            }
        }
      }
      else {
        //region SELECT CLAUSE
        var matcher = Pattern.compile(Keyword.SELECT.getPattern(), Pattern.CASE_INSENSITIVE)
            .matcher(line);

        var start = 0;
          if (matcher.find()) {
              start = matcher.end();
          } else {
              throw new UnknownError();
          }

        // synonym list not empty means - 'select clause' should contain only those declared synonyms
        if (!synonymsList.isEmpty()) {
          var selectVars = Pattern.compile(Keyword.SYNONYM.getPattern())
              .matcher(line.substring(start))
              .results()
              .map(mr -> mr.group().trim())
              .collect(Collectors.toList());

          boolean found = false;
          for (String v : selectVars) {
            for (Synonym s : synonymsList) {
              if (v.equals(s.getIdentifier())) {
                found = true;
                break;
              }
            }

              if (!found) {
                  throw new InvalidQueryException(
                      "Variable " + v + " in select clause is out of declared synonyms");
              } else {
                  found = false;
              }
          }

          var rNode = new ResultNode();

          boolean first = true;
          ResNode last = null;

          for (String v : selectVars) {

            var resNode = new ResNode(v);
            if (first) {
              resNode.setParent(rNode);
              rNode.setFirstChild(resNode);
              first = false;
            } else {
              resNode.setParent(rNode);
              last.setRightSibling(resNode);
            }

            last = resNode;
          }

          tree.setResultNode(rNode);
        }
        //endregion

        // region SUCH THAT
        matcher = Pattern.compile(Pattern.quote(Keyword.SUCH_THAT.getPattern()), Pattern.CASE_INSENSITIVE).matcher(line);

        // such that clause
        if(matcher.find())
        {
          var ststart = matcher.end();

          HashSet<Keyword> relationships = new HashSet<>(
              Arrays.asList(Keyword.FOLLOWS, Keyword.PARENT, Keyword.MODIFIES)
          );

          Keyword relType = null;

          for (Keyword rel: relationships) {
            var relMatcher = Pattern.compile(Pattern.quote(rel.getPattern()), Pattern.CASE_INSENSITIVE).matcher(line);

            if(relMatcher.find(ststart)) {
              relType = rel;
              start = matcher.end();
              break;
            }
          }

          var argsMatcher = Pattern.compile(Keyword.ARGS2.getPattern(), Pattern.CASE_INSENSITIVE).matcher(line.substring(start));

          if(!argsMatcher.find())
            throw new MissingArgumentException(relType.getPattern(), i, line);

          var args = argsMatcher.group().split(",");

          var r = new RelationshipNode(relType, args[0].trim(), args[1].trim());

          var STNode = new SuchThatNode();
          tree.getResultNode().setRightSibling(STNode);

          tree.setSuchThatNode(STNode);
          STNode.setFirstChild(r);
          r.setParent(STNode);
        }

        //endregion

        //region WITH
        matcher = Pattern.compile(Pattern.quote(Keyword.WITH.getPattern()), Pattern.CASE_INSENSITIVE).matcher(line);

        // with clause
        if(matcher.find()) {
          var wStart = matcher.end();

          var attrs = Pattern.compile(Keyword.ATTR_COND.getPattern(), Pattern.CASE_INSENSITIVE)
                  .matcher(line.substring((wStart)))
                  .results()
                  .map(mr -> mr.group().trim())
                  .collect(Collectors.toList());

          if(attrs.isEmpty())
            throw new InvalidQueryException("Invalid 'with clause'", i, line);

          /*QTNode cond = null;
          for (String attr: attrs) {
            var res = attr.split("\\.");
          }*/

          var condMatcher = Pattern.compile(Keyword.ATTR2.getPattern(), Pattern.CASE_INSENSITIVE).matcher(line.substring(wStart));

          var attrValues = condMatcher
                  .results()
                  .map(mr -> mr.group().trim())
                  .collect(Collectors.toList());

          if(attrs.size() != attrValues.size())
            throw new InvalidQueryException("Missing attribute value in 'with clause'", i, line.substring(wStart));

          // loop
          var res = attrs.get(0).split("\\.");
          var cond = new ConditionNode(res[0].trim(), res[1].trim(), attrValues.get(0));

          var wthNode = new WithNode();
          tree.setWithNode(wthNode);
          cond.setParent(cond);
          wthNode.setFirstChild(cond);

          if(tree.getSuchThatNode() != null)
            tree.getSuchThatNode().setRightSibling(wthNode);
          else
            tree.getResultNode().setRightSibling(wthNode);
        }
        //endregion
      }
    }

      if (!selectClause) {
          throw new InvalidQueryException("No select clause was found");
      }

    return tree;
  }
}

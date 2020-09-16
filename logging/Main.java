package com.github.javaparser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Optional;

import static com.github.javaparser.StaticJavaParser.parseStatement;
import static java.lang.String.format;

public class Main {
  public static void main( final String[] args ) throws FileNotFoundException {
    final File sourceFile = new File( args[ 0 ] );
    final JavaParser parser = new JavaParser();
    final ParseResult<CompilationUnit> pr = parser.parse( sourceFile );
    final Optional<CompilationUnit> ocu = pr.getResult();

    if( ocu.isPresent() ) {
      final CompilationUnit cu = ocu.get();
      final List<TypeDeclaration<?>> types = cu.getTypes();

      for( final TypeDeclaration<?> type : types ) {
        final List<MethodDeclaration> methods = type.getMethods();

        for( final MethodDeclaration method : methods ) {
          final Optional<BlockStmt> body = method.getBody();
          final String m = format( "%s::%s( %s )",
                                   type.getNameAsString(),
                                   method.getNameAsString(),
                                   method.getParameters().toString() );

          final String mBegan = format(
              "System.out.println(\"BEGAN %s\");", m );
          final String mEnded = format(
              "System.out.println(\"ENDED %s\");", m );

          final Statement sBegan = parseStatement( mBegan );
          final Statement sEnded = parseStatement( mEnded );

          body.ifPresent( ( b ) -> {
            final int i = b.getStatements().size();

            b.addStatement( 0, sBegan );

            // Insert before any "return" statement.
            b.addStatement( i, sEnded );
          } );
        }

        System.out.println( cu.toString() );
      }
    }
  }
}

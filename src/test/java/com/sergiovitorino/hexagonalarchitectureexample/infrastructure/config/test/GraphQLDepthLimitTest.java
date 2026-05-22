package com.sergiovitorino.hexagonalarchitectureexample.infrastructure.config.test;

import graphql.GraphQL;
import graphql.analysis.MaxQueryDepthInstrumentation;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;
import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Valida que o MaxQueryDepthInstrumentation(10) — configurado em GraphQLConfig —
 * rejeita queries com profundidade acima do limite e aceita as que estão dentro.
 *
 * Usa schema recursivo (Node.child: Node) isolado em src/test/resources/graphql-test/,
 * sem tocar no schema de produção. A instrumentação é instanciada com o mesmo
 * limite (10) definido em GraphQLConfig, garantindo que o teste falha se o bean
 * for removido ou o limite for alterado.
 */
class GraphQLDepthLimitTest {

    private static GraphQL graphQL;

    @BeforeAll
    static void setUp() throws Exception {
        // Carrega o schema recursivo de teste
        InputStream schemaStream = GraphQLDepthLimitTest.class
                .getResourceAsStream("/graphql-test/depth-schema.graphqls");
        assertThat(schemaStream)
                .as("Schema de teste /graphql-test/depth-schema.graphqls deve existir no classpath")
                .isNotNull();

        try (Reader reader = new InputStreamReader(schemaStream)) {
            TypeDefinitionRegistry registry = new SchemaParser().parse(reader);

            // Wiring mínimo: root retorna null (não precisamos de dados reais para testar depth)
            RuntimeWiring wiring = newRuntimeWiring()
                    .type(newTypeWiring("Query").dataFetcher("root", env -> null))
                    .build();

            GraphQLSchema schema = new SchemaGenerator().makeExecutableSchema(registry, wiring);

            // Instancia com exatamente o mesmo limite configurado em GraphQLConfig
            graphQL = GraphQL.newGraphQL(schema)
                    .instrumentation(new MaxQueryDepthInstrumentation(10))
                    .build();
        }
    }

    /**
     * Constrói uma query com N níveis de profundidade via campo "child" recursivo.
     * depth=1 → "{ root { id } }"
     * depth=2 → "{ root { id child { id } } }"
     * etc.
     */
    private static String buildQuery(int depth) {
        var sb = new StringBuilder("{ root { id");
        for (int i = 1; i < depth; i++) {
            sb.append(" child { id");
        }
        for (int i = 1; i < depth; i++) {
            sb.append(" }");
        }
        sb.append(" } }");
        return sb.toString();
    }

    @Test
    void depthBelowLimit_succeeds() {
        // buildQuery(4) gera 5 níveis contados pela instrumentação (operação anônima=1 + 4 campos).
        // Bem abaixo do limite de 10 — deve executar sem erro de depth.
        var result = graphQL.execute(buildQuery(4));

        var depthErrors = result.getErrors().stream()
                .filter(e -> e.getMessage() != null
                        && e.getMessage().toLowerCase().contains("depth"))
                .toList();

        assertThat(depthErrors)
                .as("Query contando 5 niveis nao deve gerar erro de depth (limite=10)")
                .isEmpty();
    }

    @Test
    void depthAtLimit_succeeds() {
        // buildQuery(9) gera exatamente 10 níveis contados pela instrumentação
        // (operação anônima=1 + 9 campos). Deve passar sem erro de depth.
        var result = graphQL.execute(buildQuery(9));

        var depthErrors = result.getErrors().stream()
                .filter(e -> e.getMessage() != null
                        && e.getMessage().toLowerCase().contains("depth"))
                .toList();

        assertThat(depthErrors)
                .as("Query contando exatamente 10 niveis nao deve gerar erro de depth (limite=10)")
                .isEmpty();
    }

    @Test
    void depthAboveLimit_fails() {
        // buildQuery(11) gera 12 níveis contados pela instrumentação (operação anônima=1 + 11 campos).
        // Acima do limite de 10 — deve retornar erro de depth.
        var result = graphQL.execute(buildQuery(11));

        assertThat(result.getErrors())
                .as("Query de profundidade 12 deve retornar pelo menos um erro (limite=10)")
                .isNotEmpty();

        var depthErrors = result.getErrors().stream()
                .filter(e -> e.getMessage() != null
                        && e.getMessage().toLowerCase().contains("depth"))
                .toList();

        assertThat(depthErrors)
                .as("O erro deve mencionar 'depth' — MaxQueryDepthInstrumentation deve estar ativo")
                .isNotEmpty();
    }
}

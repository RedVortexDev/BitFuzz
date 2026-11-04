package red.vortx.bitfuzz.schema.core;

import org.junit.jupiter.api.Test;
import red.vortx.bitfuzz.schema.api.SchemaBuilder;

import static org.assertj.core.api.Assertions.assertThat;

class SchemaProviderTest {

    @Test
    void createFindsCoreImplementation() {
        assertThat(SchemaBuilder.create()).isInstanceOf(SchemaBuilderImpl.class);
    }

    @Test
    void createReturnsFreshBuilders() {
        assertThat(SchemaBuilder.create()).isNotSameAs(SchemaBuilder.create());
    }

}

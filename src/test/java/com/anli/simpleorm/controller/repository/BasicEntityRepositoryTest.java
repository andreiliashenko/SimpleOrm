package com.anli.simpleorm.controller.repository;

import com.anli.simpleorm.controller.EntityRepository;
import com.anli.simpleorm.descriptors.UnitDescriptorManager;
import com.anli.simpleorm.test.MockSqlEngine;
import com.anli.simpleorm.test.TestKeyGenerator;
import org.junit.Before;

import static com.anli.simpleorm.test.TestDescriptorManagerBuilder.getTestManager;

public class BasicEntityRepositoryTest {

    protected TestKeyGenerator keyGenerator;
    protected UnitDescriptorManager descriptorManager;
    protected MockSqlEngine sqlEngine;
    protected EntityRepository repository;

    @Before
    public void setUp() {
        keyGenerator = new TestKeyGenerator();
        descriptorManager = getTestManager(keyGenerator);
        sqlEngine = new MockSqlEngine();
        repository = new BasicEntityRepository(descriptorManager, sqlEngine);
    }
}

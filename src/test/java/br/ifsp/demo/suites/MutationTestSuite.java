package br.ifsp.demo.suites;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Testes de Mutação")
@SelectPackages("br.ifsp.demo")
@IncludeTags("Mutation")
public class MutationTestSuite {
}

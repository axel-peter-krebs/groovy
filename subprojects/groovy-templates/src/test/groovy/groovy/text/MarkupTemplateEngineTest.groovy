/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.text

import groovy.text.markup.BaseTemplate
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TagLibAdapter
import groovy.text.markup.TemplateConfiguration
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail

final class MarkupTemplateEngineTest {

    private Locale locale

    @Before
    void setUp() {
        locale = Locale.default
        Locale.default = Locale.US
    }

    @After
    void tearDown() {
        Locale.default = locale
    }

    @Test
    void testSimpleTemplate() {
        def engine = new MarkupTemplateEngine(new TemplateConfiguration())
        def template = engine.createTemplate '''
            html {
                body {
                    yield 'It works!'
                }
            }
        '''
        String rendered = template.make()
        assert rendered == '<html><body>It works!</body></html>'
    }

    @Test
    // GROOVY-10731
    void testSimpleTemplate2() {
        def engine = new MarkupTemplateEngine(new TemplateConfiguration())
        def template = engine.createTemplate '''
            def map = [:]
            String key = ""
            def value = map[key] // ArrayIndexOutOfBoundsException
            span(class: "label label-${value}")
        '''
        String rendered = template.make()
        assert rendered == '<span class=\'label label-null\'/>'
    }

    @Test
    void testSimpleTemplateWithModel() {
        def engine = new MarkupTemplateEngine(new TemplateConfiguration())
        def template = engine.createTemplate '''
            html {
                body {
                    yield message
                }
            }
        '''
        String rendered = template.make(message: 'It works!')
        assert rendered == '<html><body>It works!</body></html>'
    }

    @Test
    void testSimpleTemplateWithIncludeTemplate() {
        def engine = new MarkupTemplateEngine(new TemplateConfiguration())
        def template = engine.createTemplate '''
            html {
                body {
                    include template:'includes/hello.tpl'
                }
            }
        '''
        String rendered = template.make()
        assert rendered == '<html><body>Hello from include!</body></html>'
    }

    @Test
    void testSimpleTemplateWithIncludeTemplateWithLocale() {
        def config = new TemplateConfiguration(locale: Locale.FRANCE)
        def engine = new MarkupTemplateEngine(config)
        def template = engine.createTemplate '''
            html {
                body {
                    include template:'includes/hello.tpl'
                }
            }
        '''
        String rendered = template.make()
        assert rendered == '<html><body>Bonjour!</body></html>'
    }

    @Test
    void testSimpleTemplateWithIncludeTemplateWithLocalePriority() {
        def config = new TemplateConfiguration(locale: Locale.FRANCE)
        def engine = new MarkupTemplateEngine(config)
        def template = engine.createTemplate '''
            html {
                body {
                    include template:'includes/hello_en_US.tpl' // if not found, will fall back to the default locale
                }
            }
        '''
        String rendered = template.make()
        assert rendered == '<html><body>Bonjour!</body></html>'
    }

    @Test
    void testSimpleTemplateWithIncludeRaw() {
        def engine = new MarkupTemplateEngine(new TemplateConfiguration())
        def template = engine.createTemplate '''
            html {
                body {
                    include unescaped:'includes/hello.html'
                }
            }
        '''
        StringWriter rendered = new StringWriter()
        template.make().writeTo(rendered)
        assert rendered.toString() == '<html><body>Hello unescaped!</body></html>'
    }

    @Test
    void testSimpleTemplateWithIncludeEscaped() {
        def engine = new MarkupTemplateEngine(new TemplateConfiguration())
        def template = engine.createTemplate '''
            html {
                body {
                    include escaped:'includes/hello-escaped.txt'
                }
            }
        '''
        StringWriter rendered = new StringWriter()
        template.make().writeTo(rendered)
        assert rendered.toString() == '<html><body>Hello &lt;escaped&gt;!</body></html>'
    }

    @Test
    void testHTMLHeader() {
        def engine = new MarkupTemplateEngine(new TemplateConfiguration())
        def template = engine.createTemplate '''
            yieldUnescaped '<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">'
            html {
                body('Hello, XHTML!')
            }
        '''
        StringWriter rendered = new StringWriter()
        template.make().writeTo(rendered)
        assert rendered.toString() == '<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"><html><body>Hello, XHTML!</body></html>'
    }

    @Test
    void testTemplateWithHelperMethod() {
        def engine = new MarkupTemplateEngine(new TemplateConfiguration())
        def template = engine.createTemplate '''
            def foo = {
                body('Hello from foo!')
            }

            html {
                foo()
            }
        '''
        StringWriter rendered = new StringWriter()
        template.make().writeTo(rendered)
        assert rendered.toString() == '<html><body>Hello from foo!</body></html>'
    }

    @Test
    void testCallPi() {
        def engine = new MarkupTemplateEngine(new TemplateConfiguration())
        def template = engine.createTemplate '''
            pi("xml-stylesheet":[href:"mystyle.css", type:"text/css"])
            html {
                body('Hello, PI!')
            }
        '''
        StringWriter rendered = new StringWriter()
        template.make().writeTo(rendered)
        assert rendered.toString().normalize() == '<?xml-stylesheet href=\'mystyle.css\' type=\'text/css\'?>\n<html><body>Hello, PI!</body></html>'
    }

    @Test
    void testXmlDeclaration() {
        def engine = new MarkupTemplateEngine(new TemplateConfiguration())
        def template = engine.createTemplate '''
            xmlDeclaration()
            html {
                body('Hello, PI!')
            }
        '''
        StringWriter rendered = new StringWriter()
        template.make().writeTo(rendered)
        assert rendered.toString().normalize() == '<?xml version=\'1.0\'?>\n<html><body>Hello, PI!</body></html>'
    }

    @Test
    void testXmlDeclarationWithEncoding() {
        def config = new TemplateConfiguration(declarationEncoding: 'UTF-8')
        def engine = new MarkupTemplateEngine(config)
        def template = engine.createTemplate '''
            xmlDeclaration()
            html {
                body('Hello, PI!')
            }
        '''
        StringWriter rendered = new StringWriter()
        template.make().writeTo(rendered)
        assert rendered.toString().normalize() == '<?xml version=\'1.0\' encoding=\'UTF-8\'?>\n<html><body>Hello, PI!</body></html>'
    }

    @Test
    void testNewLine() {
        def engine = new MarkupTemplateEngine(new TemplateConfiguration())
        engine.templateConfiguration.newLineString = '||'
        def template = engine.createTemplate '''
            html {
                newLine()
                body('Hello, PI!')
            }
        '''
        StringWriter rendered = new StringWriter()
        template.make().writeTo(rendered)
        assert rendered.toString() == '<html>||<body>Hello, PI!</body></html>'
    }

    @Test
    void testXMLWithYieldTag() {
        def engine = new MarkupTemplateEngine(new TemplateConfiguration())
        def template = engine.createTemplate '''
            ':yield'()
        '''
        StringWriter rendered = new StringWriter()
        template.make().writeTo(rendered)
        assert rendered.toString() == '<yield/>'
    }

    @Test
    void testTagsWithAttributes() {
        def engine = new MarkupTemplateEngine(new TemplateConfiguration())
        def template = engine.createTemplate '''
            html {
                a(href:'foo.html', 'Link text')
                tagWithQuote(attr:"fo'o")
            }
        '''
        StringWriter rendered = new StringWriter()
        template.make().writeTo(rendered)
        assert rendered.toString() == '<html><a href=\'foo.html\'>Link text</a><tagWithQuote attr=\'fo&apos;o\'/></html>'
    }

    @Test
    void testTagsWithAttributesAndDoubleQuotes() {
        def engine = new MarkupTemplateEngine(new TemplateConfiguration())
        engine.templateConfiguration.useDoubleQuotes = true
        def template = engine.createTemplate '''
            html {
                a(href:'foo.html', 'Link text')
                tagWithQuote(attr:"fo'o")
            }
        '''
        StringWriter rendered = new StringWriter()
        template.make().writeTo(rendered)
        assert rendered.toString() == '<html><a href="foo.html">Link text</a><tagWithQuote attr="fo\'o"/></html>'
    }

    @Test
    void testLoopInTemplate() {
        def engine = new MarkupTemplateEngine(new TemplateConfiguration())
        def model = [text: 'Hello', persons: ['Bob', 'Alice']]
        def template = engine.createTemplate '''
            html {
                body {
                    ul {
                        persons.each {
                            li("$text $it")
                        }
                    }
                }
            }
        '''
        StringWriter rendered = new StringWriter()
        template.make(model).writeTo(rendered)
        assert rendered.toString() == '<html><body><ul><li>Hello Bob</li><li>Hello Alice</li></ul></body></html>'
    }

    @Test
    void testHelperFunctionInBinding() {
        def engine = new MarkupTemplateEngine(new TemplateConfiguration())
        def model = [text: { it.toUpperCase() }]
        def template = engine.createTemplate '''
            html {
                body {
                    text('hello')
                }
            }
        '''
        StringWriter rendered = new StringWriter()
        template.make(model).writeTo(rendered)
        assert rendered.toString() == '<html><body>HELLO</body></html>'
    }

    @Test
    void testShouldNotEscapeUserInputAutomatically() {
        def engine = new MarkupTemplateEngine(new TemplateConfiguration())
        def model = [text: '<xml>']
        def template = engine.createTemplate '''
            html {
                body(text)
            }
        '''
        StringWriter rendered = new StringWriter()
        template.make(model).writeTo(rendered)
        assert rendered.toString() == '<html><body><xml></body></html>'
    }

    @Test
    void testShouldEscapeUserInputAutomatically() {
        def config = new TemplateConfiguration(autoEscape: true)
        def engine = new MarkupTemplateEngine(this.class.classLoader, config)
        def model = [text: '<xml>']
        def template = engine.createTemplate '''
            html {
                body(text)
            }
        '''
        StringWriter rendered = new StringWriter()
        template.make(model).writeTo(rendered)
        assert rendered.toString() == '<html><body>&lt;xml&gt;</body></html>'
    }

    @Test
    void testShouldNotEscapeUserInputAutomaticallyEvenIfFlagSet() {
        def config = new TemplateConfiguration(autoEscape: true)
        def engine = new MarkupTemplateEngine(this.class.classLoader, config)
        def model = [text: '<xml>']
        def template = engine.createTemplate '''
            html {
                body(unescaped.text)
            }
        '''
        StringWriter rendered = new StringWriter()
        template.make(model).writeTo(rendered)
        assert rendered.toString() == '<html><body><xml></body></html>'
    }

    @Test
    void testTypeCheckedModel() {
        def engine = new MarkupTemplateEngine(new TemplateConfiguration())
        def template = engine.createTypeCheckedModelTemplate '''
            html {
                body(text.toUpperCase())
            }
        ''', [text: 'String']
        def model = [text: 'Type checked!']
        StringWriter rendered = new StringWriter()
        template.make(model).writeTo(rendered)
        assert rendered.toString() == '<html><body>TYPE CHECKED!</body></html>'
    }

    @Test
    void testTypeCheckedModelShouldFail() {
        def err = shouldFail {
            def engine = new MarkupTemplateEngine(new TemplateConfiguration())
            def template = engine.createTypeCheckedModelTemplate '''
                html {
                    body(text.toUpperCase())
                }
            ''', [text: 'Integer']
            def model = [text: 'Type checked!']
            StringWriter rendered = new StringWriter()
            template.make(model).writeTo(rendered)
            assert rendered.toString() == '<html><body>TYPE CHECKED!</body></html>'
        }

        assert err =~ /Cannot find matching method java.lang.Integer#toUpperCase\(\)/
    }

    @Test
    void testTypeCheckedModelShouldFailWithoutModelDescription() {
        def err = shouldFail {
            def engine = new MarkupTemplateEngine(new TemplateConfiguration())
            def template = engine.createTypeCheckedModelTemplate '''
                html {
                    body(p.name.toUpperCase())
                }
            ''', [:]
            def model = [p: new Person(name: 'Cédric')]
            StringWriter rendered = new StringWriter()
            template.make(model).writeTo(rendered)
        }
        assert err =~ /No such property: name/
    }

    @Test
    void testTypeCheckedModelShouldSucceedWithModelDescription() {
        def engine = new MarkupTemplateEngine(new TemplateConfiguration())
        def template = engine.createTypeCheckedModelTemplate '''
            html {
                body(p.name.toUpperCase())
            }
        ''', [p: 'groovy.text.MarkupTemplateEngineTest.Person']
        def model = [p: new Person(name: 'Cedric')]
        StringWriter rendered = new StringWriter()
        template.make(model).writeTo(rendered)
        assert rendered.toString() == '<html><body>CEDRIC</body></html>'
    }

    @Test
    void testTypeCheckedModelShouldSucceedWithModelDescriptionUsingGenerics() {
        def engine = new MarkupTemplateEngine(new TemplateConfiguration())
        def template = engine.createTypeCheckedModelTemplate '''
            html {
                ul {
                    persons.each { p ->
                        li(p.name.toUpperCase())
                    }
                }
            }
        ''', [persons: 'List<groovy.text.MarkupTemplateEngineTest.Person>']
        def model = [persons: [new Person(name: 'Cedric')]]
        StringWriter rendered = new StringWriter()
        template.make(model).writeTo(rendered)
        assert rendered.toString() == '<html><ul><li>CEDRIC</li></ul></html>'
    }

    @Test
    void testTypeCheckedTemplateShouldFailInInclude() {
        def err = shouldFail {
            def engine = new MarkupTemplateEngine(new TemplateConfiguration())
            def template = engine.createTypeCheckedModelTemplate '''
                html {
                    body {
                        include template:'includes/typecheckedinclude.tpl'
                    }
                }
            ''', [text: 'Integer']
            def model = [text: 'Type checked!']
            StringWriter rendered = new StringWriter()
            template.make(model).writeTo(rendered)
        }
        assert err =~ /Cannot find matching method java.lang.Integer#toUpperCase\(\)/
    }

    @Test
    void testSimpleAutoIndent() {
        def config = new TemplateConfiguration(autoIndent: true, newLineString: '\n')
        def engine = new MarkupTemplateEngine(this.class.classLoader, config)
        def template = engine.createTemplate '''
html {
    newLine()
    body {
        newLine()
        p('Test')
        newLine()
    }
    newLine()
}
'''
        StringWriter rendered = new StringWriter()
        template.make().writeTo(rendered)
        assert rendered.toString() == '''\
<html>
    <body>
        <p>Test</p>
    </body>
</html>'''
    }

    @Test
    void testSimpleAutoIndentShouldAddNewLineInLoop() {
        def config = new TemplateConfiguration(autoIndent: true, newLineString: '\n')
        def engine = new MarkupTemplateEngine(this.class.classLoader, config)
        def template = engine.createTemplate '''
html {
    newLine()
    body {
        newLine()
        ul {
            newLine()
            persons.eachWithIndex { p,i ->
                if (i) newLine()
                li(p)
            }
            newLine()
        }
        newLine()
    }
    newLine()
}
'''
        StringWriter rendered = new StringWriter()
        def model = [persons: ['Cedric', 'Jochen']]
        template.make(model).writeTo(rendered)
        assert rendered.toString() == '''\
<html>
    <body>
        <ul>
            <li>Cedric</li>
            <li>Jochen</li>
        </ul>
    </body>
</html>'''
    }

    @Test
    void testSimpleAutoIndentShouldAutoAddNewLineInLoop() {
        def config = new TemplateConfiguration(autoIndent: true, autoNewLine: true, newLineString: '\n')
        def engine = new MarkupTemplateEngine(this.class.classLoader, config)
        def template = engine.createTemplate '''
html {
    body {
        ul {
            persons.each {
                li(it)
                newLine()
            }
        }
    }
}
'''
        StringWriter rendered = new StringWriter()
        def model = [persons: ['Cedric', 'Jochen']]
        template.make(model).writeTo(rendered)
        assert rendered.toString() == '''\
<html>
    <body>
        <ul>
            <li>Cedric</li>
            <li>Jochen</li>
            |
        </ul>
    </body>
</html>'''.replace('|', '')
    }

    @Test
    void testSimpleAutoIndentWithAutoNewLine() {
        def config = new TemplateConfiguration(autoIndent: true, autoNewLine: true, newLineString: '\n')
        def engine = new MarkupTemplateEngine(this.class.classLoader, config)
        def template = engine.createTemplate '''
html {
    body {
        p('Test')
    }
}
'''
        StringWriter rendered = new StringWriter()
        template.make().writeTo(rendered)
        assert rendered.toString() == '''\
<html>
    <body>
        <p>Test</p>
    </body>
</html>'''
    }

    @Test
    void testCustomTemplateClass() {
        TemplateConfiguration config = new TemplateConfiguration()
        config.baseTemplateClass = CustomBaseTemplate
        MarkupTemplateEngine engine = new MarkupTemplateEngine(this.class.classLoader, config)
        def template = engine.createTemplate '''p(getVersion())'''
        StringWriter rendered = new StringWriter()
        def tpl = template.make()
        tpl.version = 'Template v1'
        tpl.writeTo(rendered)
        assert rendered.toString() == "<p>Template v1</p>"
    }

    @Test
    void testShouldNotThrowTypeCheckingError() {
        TemplateConfiguration config = new TemplateConfiguration()
        MarkupTemplateEngine engine = new MarkupTemplateEngine(this.class.classLoader, config)
        def template = engine.createTemplate '''int x = name.length()
            yield "$name: $x"
        '''
        StringWriter rendered = new StringWriter()
        def model = [name: 'Michel']
        def tpl = template.make(model)
        tpl.writeTo(rendered)
        assert rendered.toString() == "Michel: 6"
    }

    @Test
    void testComment() {
        TemplateConfiguration config = new TemplateConfiguration()
        MarkupTemplateEngine engine = new MarkupTemplateEngine(this.class.classLoader, config)
        def template = engine.createTemplate '''comment " This is a $comment "'''
        StringWriter rendered = new StringWriter()
        def model = [comment: 'comment']
        def tpl = template.make(model)
        tpl.writeTo(rendered)
        assert rendered.toString() == "<!-- This is a comment -->"
    }

    @Test
    void testYieldUnescaped() {
        TemplateConfiguration config = new TemplateConfiguration()
        MarkupTemplateEngine engine = new MarkupTemplateEngine(this.class.classLoader, config)
        def template = engine.createTemplate '''yieldUnescaped html'''
        StringWriter rendered = new StringWriter()
        def model = [html: '<html></html>']
        def tpl = template.make(model)
        tpl.writeTo(rendered)
        assert rendered.toString() == "<html></html>"
    }

    @Test
    void testGrailsTagLibCompatibility() {
        TemplateConfiguration config = new TemplateConfiguration()
        MarkupTemplateEngine engine = new MarkupTemplateEngine(this.class.classLoader, config)
        def template = engine.createTemplate '''g.emoticon(happy:'true') { 'Hi John' }'''
        StringWriter rendered = new StringWriter()
        def model = [:]
        def tpl = template.make(model)
        model.g = new TagLibAdapter(tpl)
        model.g.registerTagLib(SimpleTagLib)
        tpl.writeTo(rendered)
        assert rendered.toString() == "Hi John :-)"
    }

    @Test
    void testLoadTemplateFromDirectory() {
        def tplDir = File.createTempDir("templates", "")
        try {
            def templateFile = new File(tplDir, "hello-from-dir.tpl")

            def loader = this.class.classLoader
            templateFile << loader.getResourceAsStream('includes/hello.tpl')
            assert templateFile.text
            MarkupTemplateEngine engine = new MarkupTemplateEngine(loader, tplDir, new TemplateConfiguration())
            def template = engine.createTemplate '''
                html {
                    body {
                        include template:'hello-from-dir.tpl'
                    }
                }
            '''
            StringWriter rendered = new StringWriter()
            template.make().writeTo(rendered)
            assert rendered.toString() == '<html><body>Hello from include!</body></html>'
        } finally {
            tplDir.deleteDir()
        }
    }

    @Test
    void testLoadTemplateByName() {
        TemplateConfiguration config = new TemplateConfiguration()
        MarkupTemplateEngine engine = new MarkupTemplateEngine(this.class.classLoader, config)
        def template = engine.createTemplateByPath 'includes/hello.tpl'
        StringWriter rendered = new StringWriter()
        def model = [:]
        def tpl = template.make(model)
        tpl.writeTo(rendered)
        assert rendered.toString() == "Hello from include!"
    }

    @Test
    void testLoadTemplateByNameWithLocale() {
        TemplateConfiguration config = new TemplateConfiguration()
        config.locale = Locale.FRANCE
        MarkupTemplateEngine engine = new MarkupTemplateEngine(this.class.classLoader, config)
        def template = engine.createTemplateByPath 'includes/hello.tpl'
        StringWriter rendered = new StringWriter()
        def model = [:]
        def tpl = template.make(model)
        tpl.writeTo(rendered)
        assert rendered.toString() == "Bonjour!"
    }

    @Test
    void testTypeCheckedModelShouldNotConflictWithAutoEscape() {
        def model = [title: "This is my glorious title ${1 + 1}".toString()]
        def template = new MarkupTemplateEngine(this.class.classLoader, new TemplateConfiguration(autoNewLine: true, autoEscape: true, newLineString: 'NL')).createTypeCheckedModelTemplate('''
            body {
              div(class: 'text')  {
                yield title.toUpperCase()
              }
            }
        ''', [title: 'String'])
        def stringWriter = new StringWriter()
        template.make(model).writeTo(stringWriter)
        assert stringWriter.toString() == '<body>NL<div class=\'text\'>NLTHIS IS MY GLORIOUS TITLE 2NL</div>NL</body>'
    }

    @Test
    void testCopyConstructorForTemplateConfiguration() {
        TemplateConfiguration cfg = new TemplateConfiguration(
            declarationEncoding: 'iso-8859-1',
            expandEmptyElements: true,
            useDoubleQuotes: false,
            newLineString: 'NL',
            autoEscape: true,
            autoIndent: true,
            autoIndentString: true,
            autoNewLine: true,
            baseTemplateClass: BaseTemplate,
            locale: Locale.CHINA
        )
        def copy = new TemplateConfiguration(cfg)
        cfg.properties.each {
            String pName = it.key
            assert cfg."$pName" == copy."$pName"
        }
    }

    @Test
    void testCollectionInModel() {
        MarkupTemplateEngine engine = new MarkupTemplateEngine(new TemplateConfiguration())
        def template = engine.createTemplate '''
            html {
                body {
                    ul {
                        persons.each { p ->
                            li(p.name)
                        }
                    }
                }
            }
        '''
        StringWriter rendered = new StringWriter()
        def model = [persons: [[name: 'Cedric'], [name: 'Jochen']]]
        template.make(model).writeTo(rendered)
        assert rendered.toString() == '<html><body><ul><li>Cedric</li><li>Jochen</li></ul></body></html>'
    }

    @Test
    @Ignore("ClassCastException: java.util.LinkedHashMap cannot be cast to groovy.lang.GroovyObject")
    void testInlinedModelTypeDeclaration() {
        MarkupTemplateEngine engine = new MarkupTemplateEngine(new TemplateConfiguration())
        def template = engine.createTemplate '''
            modelTypes = {
                List<groovy.text.MarkupTemplateEngineTest.Person> persons
            }

            html {
                body {
                    ul {
                        persons.each { p ->
                            li(p.name)
                        }
                    }
                }
            }
        '''
        StringWriter rendered = new StringWriter()
        def model = [persons: [[name: 'Cedric'], [name: 'Jochen']]]
        template.make(model).writeTo(rendered)
        assert rendered.toString() == '<html><body><ul><li>Cedric</li><li>Jochen</li></ul></body></html>'
    }

    @Test
    void testInlinedModelTypeDeclarationShouldFailBecauseIncorrectType() {
        def err = shouldFail {
            MarkupTemplateEngine engine = new MarkupTemplateEngine(new TemplateConfiguration())
            def template = engine.createTemplate '''
                modelTypes = {
                    List<String> persons
                }

                html {
                    body {
                        ul {
                            persons.each { p ->
                                li(p.name)
                            }
                        }
                    }
                }
            '''
            StringWriter rendered = new StringWriter()
            def model = [persons: [[name: 'Cedric'], [name: 'Jochen']]]
            template.make(model).writeTo(rendered)
            assert rendered.toString() == '<html><body><ul><li>Cedric</li><li>Jochen</li></ul></body></html>'
        }
        assert err =~ /No such property: name for class: java.lang.String/
    }

    @Test
    void testFragment() {
        def config = new TemplateConfiguration()
        MarkupTemplateEngine engine = new MarkupTemplateEngine(config)
        def template = engine.createTemplate '''
            html {
                body {
                    [Index: 'index.html',
                    Page1: 'page.html',
                    Page2: 'page2.html'].each { k,v ->
                        fragment(page:v,title:k, 'a(href:page, title)')
                    }
                }
            }
        '''
        StringWriter rendered = new StringWriter()
        template.make().writeTo(rendered)
        assert rendered.toString() == "<html><body><a href='index.html'>Index</a><a href='page.html'>Page1</a><a href='page2.html'>Page2</a></body></html>"
    }

    @Test
    void testLayout() {
        def config = new TemplateConfiguration()
        MarkupTemplateEngine engine = new MarkupTemplateEngine(config)
        def template = engine.createTemplate '''
            layout 'includes/body.tpl', bodyContents: contents {
                div {
                    p('This is the body')
                }
            }, title: 'This is the title'
        '''
        StringWriter rendered = new StringWriter()
        template.make().writeTo(rendered)
        assert rendered.toString() == "<html><head><title>This is the title</title></head><body><div><p>This is the body</p></div></body></html>"
    }

    @Test
    // GROOVY-6915
    void testLayoutWithModelInheritance() {
        def config = new TemplateConfiguration()
        MarkupTemplateEngine engine = new MarkupTemplateEngine(config)
        def template = engine.createTemplate '''
            layout 'includes/body.tpl', bodyContents: contents {
                div {
                    p('This is the body')
                }
            }
        '''
        StringWriter rendered = new StringWriter()
        template.make([title: 'This is the title']).writeTo(rendered)
        assert rendered.toString() == "<html><head><title/></head><body><div><p>This is the body</p></div></body></html>"

        template = engine.createTemplate '''
            layout 'includes/body.tpl', true, bodyContents: contents {
                div {
                    p('This is the body')
                }
            }
        '''
        rendered = new StringWriter()
        template.make([title: 'This is the title']).writeTo(rendered)
        assert rendered.toString() == "<html><head><title>This is the title</title></head><body><div><p>This is the body</p></div></body></html>"

        template = engine.createTemplate '''
            layout 'includes/body.tpl', true, bodyContents: contents {
                div {
                    p('This is the body')
                }
            }, title: 'This is another title'
        '''
        rendered = new StringWriter()
        template.make([title: 'This is the title']).writeTo(rendered)
        assert rendered.toString() == "<html><head><title>This is another title</title></head><body><div><p>This is the body</p></div></body></html>"
    }

    @Test
    void testSimplePIRenderedProperly() {
        def config = new TemplateConfiguration()
        config.newLineString = ''
        MarkupTemplateEngine engine = new MarkupTemplateEngine(config)
        def template = engine.createTemplate '''pi(FOO: 'bar')'''
        StringWriter rendered = new StringWriter()
        template.make().writeTo(rendered)
        assert rendered.toString() == '<?FOO bar?>'
    }

    @Test
    // GROOVY-6794
    void testShouldNotThrowForbiddenPropertyAccess() {
        def config = new TemplateConfiguration()
        MarkupTemplateEngine engine = new MarkupTemplateEngine(config)
        def template = engine.createTemplate '''messages.each { message ->
                yield message.summary
            }
        '''
        StringWriter rendered = new StringWriter()
        def model = [messages: [new Message(summary: 'summary')]]
        template.make(model).writeTo(rendered)
        assert rendered.toString() == 'summary'
    }

    @Test
    // GROOVY-6914
    void testCachingOfTemplateResolver() {
        int hit = 0
        int miss = 0
        def cache = new HashMap<String, URL>() {
            @Override
            URL get(final Object key) {
                URL url = super.get(key)
                if (url) {
                    hit++
                } else {
                    miss++
                }
                url
            }
        }
        def resolver = new MarkupTemplateEngine.CachingTemplateResolver(cache)
        MarkupTemplateEngine engine = new MarkupTemplateEngine(this.class.classLoader, new TemplateConfiguration(), resolver)
        def template = engine.createTemplate '''
            html {
                body {
                    include template:'includes/hello.tpl'
                    include template:'includes/hello.tpl'
                    include template:'includes/hello.tpl'
                }
            }
        '''
        StringWriter rendered = new StringWriter()
        template.make().writeTo(rendered)
        assert rendered.toString() == '<html><body>Hello from include!Hello from include!Hello from include!</body></html>'
        assert miss == 1
        assert hit == 2
    }

    @Test
    void testMarkupInGString() {
        MarkupTemplateEngine engine = new MarkupTemplateEngine(new TemplateConfiguration())
        def template = engine.createTemplate '''
            html {
                body {
                    def test = { p('hg') }
                    def x = "directly ${$test()}"
                    p("This is a p with ${true?$a(href:'link.html','link'):x}")
                    p("This is a p with ${false?$a(href:'link.html','link'):x}")
                }
            }
        '''
        String rendered = template.make()
        assert rendered == '<html><body><p>This is a p with <a href=\'link.html\'>link</a></p><p>This is a p with directly <p>hg</p></p></body></html>'
    }

    @Test
    void testMarkupInGStringUsingStringOf() {
        MarkupTemplateEngine engine = new MarkupTemplateEngine(new TemplateConfiguration())
        def template = engine.createTemplate '''
            html {
                body {
                    def test = { p('hg') }
                    def x = "directly ${stringOf { test()} }"
                    p("This is a p with ${stringOf { true?a(href:'link.html','link'):x} }")
                    p("This is a p with ${stringOf { false?a(href:'link.html','link'):x} }")
                }
            }
        '''
        String rendered = template.make()
        assert rendered == '<html><body><p>This is a p with <a href=\'link.html\'>link</a></p><p>This is a p with directly <p>hg</p></p></body></html>'
    }

    @Test
    void testShouldNotThrowStackOverflow() {
        MarkupTemplateEngine engine = new MarkupTemplateEngine(new TemplateConfiguration())
        def template = engine.createTemplate '''
            p("This is an ${strong('error')}")
        '''
        String rendered = template.make().writeTo(new StringWriter())
        assert rendered == '<strong>error</strong><p>This is an </p>'
    }

    @Test
    // GROOVY-6935
    void testShouldNotThrowVerifyErrorBecauseOfEqualsInsteadOfSemiColumn() {
        MarkupTemplateEngine engine = new MarkupTemplateEngine(new TemplateConfiguration())
        def template = engine.createTemplate '''
            a(href='foo.html', 'link')
        '''
        try {
            template.make().writeTo(new StringWriter())
            assert false
        } catch (UnsupportedOperationException e) {
            assert true
        }
        def model = [:]
        String rendered = template.make(model).writeTo(new StringWriter())
        assert model.href == 'foo.html'
        assert rendered == '<a>link</a>'
    }

    @Test
    // GROOVY-6939
    void testShouldNotFailWithDoCallMethod() {
        MarkupTemplateEngine engine = new MarkupTemplateEngine(new TemplateConfiguration())
        def template = engine.createTemplate '''
            groups.each { k, v -> li(k) }
        '''
        def model = [groups: [a: 'Group a', b: 'Group b']]
        String rendered = template.make(model)
        assert rendered == '<li>a</li><li>b</li>'
    }

    @Test
    // GROOVY-6940
    void testSubscriptOperatorOnModel() {
        MarkupTemplateEngine engine = new MarkupTemplateEngine(new TemplateConfiguration())
        def template = engine.createTemplate '''
            yield list[0]
        '''
        def model = [list: ['Item 1']]
        String rendered = template.make(model)
        assert rendered == 'Item 1'

        template = engine.createTemplate '''
            list[0] = 'Item 2'
            yield list[0]
        '''
        model = [list: ['Item 1']]
        rendered = template.make(model)
        assert model.list[0] == 'Item 2'
        assert rendered == 'Item 2'

        template = engine.createTemplate '''
            def indirect = list
            indirect[0] = 'Item 4'
            yield list[0]
        '''
        model = [list: ['Item 3']]
        rendered = template.make(model)
        assert model.list[0] == 'Item 4'
        assert rendered == 'Item 4'
    }

    @Test
    // GROOVY-6941
    void testDynamicPropertyInsideBlock() {
        MarkupTemplateEngine engine = new MarkupTemplateEngine(new TemplateConfiguration())
        def template = engine.createTemplate '''
            div {
                yield xml.file.name
            }
        '''
        def model = [xml: [file: [name: 'test']]]
        String rendered = template.make(model)
        assert rendered == '<div>test</div>'
    }

    //--------------------------------------------------------------------------

    static class SimpleTagLib {
        def emoticon = { attrs, body ->
            out << body() << (attrs.happy == 'true' ? " :-)" : " :-(")
        }
    }

    static class Person {
        String name
    }

    private static class Message {
        private String summary

        String getSummary() {
            return summary
        }

        void setSummary(final String summary) {
            this.summary = summary
        }
    }
}

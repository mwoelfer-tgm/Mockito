package Woelfer;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.*;
import static org.mockito.Matchers.*;
import static org.mockito.hamcrest.MockitoHamcrest.*;

public class MockitoTest {
	LinkedList mockedList;
	@Before
	public void setUp() throws Exception {
		mockedList = mock(LinkedList.class);
	}

	@After
	public void tearDown() throws Exception {
	}
	
	
	@Test
	public void testBehaviour() {
		List mockedList = mock(List.class);
		//using mock object
		mockedList.add("one");
		mockedList.clear();
		 
		//verification
		verify(mockedList).add("one");
		verify(mockedList).clear();
	}
	
	@Test(expected=RuntimeException.class)
	public void testStubbing(){
		//You can mock concrete classes, not just interfaces
		 LinkedList mockedList = mock(LinkedList.class);

		 //stubbing
		 when(mockedList.get(0)).thenReturn("first");
		 when(mockedList.get(1)).thenThrow(new RuntimeException());

		 //following prints "first"
		 System.out.println(mockedList.get(0));

		 //following throws runtime exception
		 System.out.println(mockedList.get(1));

		 //following prints "null" because get(999) was not stubbed
		 System.out.println(mockedList.get(999));

		 //Although it is possible to verify a stubbed invocation, usually it's just redundant
		 //If your code cares what get(0) returns, then something else breaks (often even before verify() gets executed).
		 //If your code doesn't care what get(0) returns, then it should not be stubbed. Not convinced? See here.
		 verify(mockedList).get(0);
	}
	
	@Test
	public void testArgumentMatcher(){
		 //stubbing using built-in anyInt() argument matcher
		 when(mockedList.get(anyInt())).thenReturn("element");

		 //stubbing using custom matcher (let's say isValid() returns your own matcher implementation):
		 //when(mockedList.contains(argThat(isValid()))).thenReturn("element");

		 //following prints "element"
		 System.out.println(mockedList.get(999));

		 //you can also verify using an argument matcher
		 verify(mockedList).get(anyInt());
	}
	
	@Test
	public void testNumberOfInovotaions(){
		 //using mock
		 mockedList.add("once");

		 mockedList.add("twice");
		 mockedList.add("twice");

		 mockedList.add("three times");
		 mockedList.add("three times");
		 mockedList.add("three times");

		 //following two verifications work exactly the same - times(1) is used by default
		 verify(mockedList).add("once");
		 verify(mockedList, times(1)).add("once");

		 //exact number of invocations verification
		 verify(mockedList, times(2)).add("twice");
		 verify(mockedList, times(3)).add("three times");

		 //verification using never(). never() is an alias to times(0)
		 verify(mockedList, never()).add("never happened");

		 //verification using atLeast()/atMost()
		 verify(mockedList, atLeastOnce()).add("three times");
		 verify(mockedList, atLeast(2)).add("twice");
		 verify(mockedList, atMost(5)).add("three times");
	}
	
	//mit der methode doThrow() kann man sagen welche Exception geworfen werden soll wenn (=> .when()) etwas bestimmtes gemacht wird
	@Test
	public void testMethodWithExceptions(){
		doThrow(new RuntimeException()).when(mockedList).clear();

	   //following throws RuntimeException:
	   mockedList.clear();
	}
	
	/*
	 * Zuerst wird ein mock der Klasse List erstellt, in welchem dann 2 Strings geadded werden
	 * Danach wird ein inOrder objekt erzeugt, welches als Parameter im Konstruktor jene objekte nimmt die es überprüfen soll
	 * In nächsten Block wird dann tatsächlich überprüft ob die Strings in der richtigen Reihenfolge geadded wurden
	 * 
	 * Als nächstes werden 2 mocks der Klasse List erstellt, in welche jeweils 1 String geadded wird
	 * Danach wird wieder ein inOrder objekt erzeugt, welches wieder als Parameter die Objekte nimmt die es überprüft
	 * Nun kann man wieder überprüfen ob tatsächlich zuerst der eine String und dann der andere geadded wurde
	 */
	@Test
	public void testOrder(){
		 // A. Single mock whose methods must be invoked in a particular order
		 List singleMock = mock(List.class);

		 //using a single mock
		 singleMock.add("was added first");
		 singleMock.add("was added second");

		 //create an inOrder verifier for a single mock
		 InOrder inOrder = inOrder(singleMock);

		 //following will make sure that add is first called with "was added first, then with "was added second"
		 inOrder.verify(singleMock).add("was added first");
		 inOrder.verify(singleMock).add("was added second");

		 // B. Multiple mocks that must be used in a particular order
		 List firstMock = mock(List.class);
		 List secondMock = mock(List.class);

		 //using mocks
		 firstMock.add("was called first");
		 secondMock.add("was called second");

		 //create inOrder object passing any mocks that need to be verified in order
		 InOrder inOrder2 = inOrder(firstMock, secondMock);

		 //following will make sure that firstMock was called before secondMock
		 inOrder2.verify(firstMock).add("was called first");
		 inOrder2.verify(secondMock).add("was called second");

	}

	/*
	 * Zuerst werden 3 Mocks der klasse List erstellet
	 * Danach wird eine ganz normale Überprüfung durchgeführt ob der String eh geaddded wurde
	 * Überpüfen dass auf dem Mock nie eine andere Methode ausgeführt wurde
	 * 
	 * Zum schluss überprüfen ob es Interaktionen mit den anderen Mocks gab mit : VerifyZeroInteractions()
	 */
	@Test
	public void testInteractions(){
		List mockOne = mock(List.class);
		List mockTwo = mock(List.class);
		List mockThree = mock(List.class);
		//using mocks - only mockOne is interacted
		 mockOne.add("one");

		 //ordinary verification
		 verify(mockOne).add("one");

		 //verify that method was never called on a mock
		 verify(mockOne, never()).add("two");

		 //verify that other mocks were not interacted
		 verifyZeroInteractions(mockTwo, mockThree);
	}
	
	/*
	 * Zuerst werden zur LinkedList zwei elemente eingefügt, dann wird ordinär überprüft ob es hinzugefügt wurde
	 * Dann wurde mit der Methode verifyNoMoreInteractions() überprüft ob sonst "nichts passiert" ist,
	 * was falsch ist, da wir noch "two" geadded haben, also failed der test!
	 */
	@Test
	public void testRedundantInvocations(){

		 //using mocks
		 mockedList.add("one");
		 mockedList.add("two");

		 verify(mockedList).add("one");

		 //following verification will fail
		 verifyNoMoreInteractions(mockedList);
	}
	
	/*
	 * Zuerst wurde eine Klasser erstellt wo verschiedene beliebige Attribute erstellt wurden mit der
	 * Annotation @Mock
	 * Dann wurde aus der Klasse MockitoAnnotations die Methode initMocks() ausgeführt welche als Parameter ein Objekt
	 * von der Klasse nimmt wo die Attribute initinialisiert werden soll, dadurch kann man statt
	 * Integer mock = mock(Integer.class); einfach @Mock Integer mock; schreiben
	 * 
	 */
	@Test
	public void testShorthandMockCreation(){
		MockitoAnnotations.initMocks(new annotationTest());
	}
	
	/*
	 * Es wurde am anfang wieder gestubbed, wenn nun die Methode get(0) aufgerufen wird, wird zuerst
	 * eine RunTimeException geworfen und dann"SEW FTW" geprintet
	 * 
	 * Falls man danach nochmal get(0) aufruft, wird wieder geprintet => "Last stubbing wins"
	 */
	@Test
	public void testConsecutiveStubbing(){
		 when(mockedList.get(0))
		   .thenThrow(new RuntimeException())
		   .thenReturn("SEW FTW");

		 //First call: throws runtime exception:
		 mockedList.get(0);

		 //Second call: prints "SEW FTW"
		 System.out.println(mockedList.get(0));

		 //Any consecutive call: prints "SEW FTW" as well (last stubbing wins).
		 System.out.println(mockedList.get(0));
	}
	
	/*
	 * Zuerst wird normal gestubbt, aber diesmal kommt kein thenReturn() oder etliches, sondern thenAnswer()
	 * als nächstes erstellt mein ein Answer objekt, welches kurz darauf implementiert wurde.
	 * 
	 * Answer nimmt als parameter die InvocationOnMock, damit ist gemeint z.B: mockedList.add("test")
	 * und dies wird dann mit einem kleinen text returned
	 * 
	 * Wenn jetzt nun .add("test") aufgerufen wird, wird "called with arguments: test" ausgegeben
	 */
	@Test
	public void testStubbingWithCallbacks(){
		when(mockedList.add("test")).thenAnswer(new Answer() {
		     public Object answer(InvocationOnMock invocation) {
		         Object[] args = invocation.getArguments();
		         return "called with arguments: " + args;
		     }
		 });
		
		System.out.println(mockedList.add("test"));
	}
	
	/*
	 * Ein Spy ist eine Art mix aus einem Mock und einer echten Klasse. zuerst wird der ein objekt der klasse List
	 * mit der methode spy() erzeugt. Danach wird gestubbed, das wenn spy.size() aufgerufen wird 100 ausgegeben wird
	 * Wenn man jetzt nun Methoden auf das Spy objekt ausführt werden die "echten" Methoden ausgeführt, aber wenn man 
	 * eine gestubbte Methode aufruft wird dann z.B. 100 ausgegeben
	 * 
	 *  Man sollte bei Spying objects nicht when.then verwenden sondern eher do.when
	 *  Beispiel: 
   	 *			when(spy.get(0)).thenReturn("foo"); => wirft IndexOutOfBoundsException
   	 *			doReturn("foo").when(spy).get(0); => funktioniert
	 */
	@Test
	public void testSpyObjects(){
		   List spy = spy(new LinkedList());

		   //optionally, you can stub out some methods:
		   when(spy.size()).thenReturn(100);

		   //using the spy calls *real* methods
		   spy.add("one");
		   spy.add("two");

		   //prints "one" - the first element of a list
		   System.out.println(spy.get(0));

		   //size() method was stubbed - 100 is printed
		   System.out.println(spy.size());

		   //optionally, you can verify
		   verify(spy).add("one");
		   verify(spy).add("two");
	}
}

package dev.txd.wordguesser;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class MainTest {
    
    @Test 
    void TestSanitizeWordWithAccentsAndCase() {
        String result = callPrivateMethod(Main.class, "sanitizeWord", new Class<?>[]{String.class}, "músIca");
        assertEquals("MUSICA", result);
    }



    @ParameterizedTest 
    @CsvSource({
        // letter, position, answer, expectedAnsiColor
        "A, 0, APPLE, '\u001B[42m\u001B[30m'", 
        "E, 0, APPLE, '\u001B[43m\u001B[30m'", 
        "Z, 0, APPLE, ''"                       
    })
    void testGetLetterColorAllBranches(char letter, int position, String answer, String expectedColor) throws Exception {
        String result = callPrivateMethod(Main.class, 
            "getLetterColor", 
            new Class<?>[]{ char.class, int.class, String.class}, 
            letter, position, answer
        );

        assertEquals(expectedColor, result);
    }
    
    @SuppressWarnings("unchecked")
    private <T> T callPrivateMethod(Class<?> clazz, String methodName, Class<?>[] paramTypes, Object... args) {
        Method method = null;
        try {
            method = clazz.getDeclaredMethod(methodName, paramTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException();
        }
        method.setAccessible(true);
        T result = null;
        try { 
            result = (T) method.invoke(null, args); 
        } catch (IllegalAccessException | InvocationTargetException e) {}
        method.setAccessible(false);
        return result;
    }

}

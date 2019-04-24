package edu.northeastern.ccs.im;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *a test case to test Exceptions
 */
class NextDoesNotExistExceptionTest {

    /**
     *testing if an exception is thrown
     */
    @Test
            void exception() {
                assertThrows(NextDoesNotExistException.class,
                () -> {
                    throw new NextDoesNotExistException("No next line has been typed in at the keyboard");
                      }

                );

            }





}
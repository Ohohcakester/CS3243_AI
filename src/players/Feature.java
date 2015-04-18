package players;

import main.NextState;

/**
 * Functional interface for feature functions
 */
public interface Feature {
    float compute(NextState n);
}

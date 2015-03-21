package players;

import main.NextState;
import main.State;

public interface Feature {
    float compute(NextState n);
}
package players;

import main.NextState;

public interface Feature {
    float compute(NextState n);
}
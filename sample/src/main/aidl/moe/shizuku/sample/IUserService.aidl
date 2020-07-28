package moe.shizuku.sample;

interface IUserService {

    void cleanup() = 16777114; // Reserved cleanup method

    void exit() = 1;

    int getPid() = 2;
}